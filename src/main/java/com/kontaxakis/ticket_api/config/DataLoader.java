package com.kontaxakis.ticket_api.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.kontaxakis.ticket_api.entity.User;
import com.kontaxakis.ticket_api.entity.Event;
import com.kontaxakis.ticket_api.entity.EventStatus;

import com.kontaxakis.ticket_api.repository.EventRepository;
import com.kontaxakis.ticket_api.repository.UserRepository;

import com.kontaxakis.ticket_api.service.RedisStockService;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@AllArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RedisStockService redisStockService;

    /**
     * Automatically injects test data on startup for local API testing.
     */
    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User user = new User();
            user.setUsername("testUsername");
            user.setEmail("test@gmail.com");
            user.setCreatedAt(Instant.now());
            userRepository.save(user);
        }
        if (eventRepository.count() == 0) {
            Event event = new Event();
            event.setTitle("Test Concert");
            event.setDescription("Test Description");
            event.setEventDate(Instant.now().plus(java.time.Duration.ofDays(10)));
            event.setTotalTickets(1000);
            event.setAvailableTickets(1000);
            event.setPrice(BigDecimal.valueOf(50.00));
            event.setStatus(EventStatus.ACTIVE);
            eventRepository.save(event);

            redisStockService.initializeStock(event.getId(), event.getAvailableTickets());
        }
    }

}
