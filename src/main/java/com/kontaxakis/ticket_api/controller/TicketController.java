package com.kontaxakis.ticket_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.ResponseEntity;

import com.kontaxakis.ticket_api.service.RedisStockService;
import com.kontaxakis.ticket_api.service.RabbitMQProducer;

import com.kontaxakis.ticket_api.entity.Order;
import com.kontaxakis.ticket_api.entity.OrderStatus;
import com.kontaxakis.ticket_api.entity.User;
import com.kontaxakis.ticket_api.entity.Event;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    public record BuyTicketRequest(@NotNull Long userId, @NotNull Long eventId, @Min(1) @Max(10) int quantity) {
    }

    private final RedisStockService redisStockService;
    private final RabbitMQProducer rabbitMQProducer;

    /**
     * To handle flash sale traffic the method never touches the database.
     * It only checks the cache and immediately pushes the order to RabbitMQ.
     */
    @PostMapping("/buy")
    public ResponseEntity<String> buyTicket(@Valid @RequestBody BuyTicketRequest request) {
        long startTime = System.currentTimeMillis();
        boolean isReserved = redisStockService.reserveTickets(request.eventId(), request.quantity());
        if (!isReserved) {
            return ResponseEntity.badRequest().body("Sold out. Order failed.");
        }

        User user = new User();
        user.setId(request.userId());

        Event event = new Event();
        event.setId(request.eventId());

        Order order = new Order();
        order.setEvent(event);
        order.setUser(user);
        order.setTicketCount(request.quantity());
        order.setTotalAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.PENDING);
        order.setFailureReason(null);
        order.setCreatedAt(Instant.now());

        rabbitMQProducer.sendOrder(order);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        return ResponseEntity.ok("Order accepted and is processing. Processed in " + duration + "ms");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions() {
        return ResponseEntity.badRequest()
                .body("Order failed. Reason: The valid number of tickets is between 1 and 10.");
    }

}
