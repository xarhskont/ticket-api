package com.kontaxakis.ticket_api.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.kontaxakis.ticket_api.entity.Event;
import com.kontaxakis.ticket_api.entity.Order;
import com.kontaxakis.ticket_api.entity.OrderStatus;
import com.kontaxakis.ticket_api.config.RabbitMQConfig;
import com.kontaxakis.ticket_api.repository.OrderRepository;
import com.kontaxakis.ticket_api.repository.EventRepository;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final OrderRepository orderRepository;
    private final EventRepository eventRepository;

    /**
     * Listens to the RabbitMQ queue and saves incoming orders to the database.
     */
    @Transactional
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveOrder(Order order) {
        Event event = eventRepository.findById(order.getEvent().getId()).orElseThrow();
        order.setEvent(event);
        if (event.getAvailableTickets() < order.getTicketCount()) {
            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason("Not enough tickets");
            orderRepository.save(order);
            return;
        }
        event.setAvailableTickets(event.getAvailableTickets() - order.getTicketCount());
        eventRepository.save(event);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(BigDecimal.valueOf(order.getTicketCount()).multiply(event.getPrice()));
        orderRepository.save(order);
    }

}
