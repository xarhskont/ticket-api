package com.kontaxakis.ticket_api.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisStockService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Sets the initial stock for a given event.
     */
    public void initializeStock(Long eventId, Integer totalTickets) {
        redisTemplate.opsForValue().set("event:stock:" + eventId, totalTickets.toString());
    }

    /**
     * Attempts to reserve tickets for a given event.
     * Decrements the stock first, and if the stock drops below zero,
     * rolls back the transaction by incrementing it back.
     * Returns true if reserved successfully, false if sold out.
     */
    public boolean reserveTickets(Long eventId, Integer ticketCount) {
        String key = "event:stock:" + eventId;
        Long remainingStock = redisTemplate.opsForValue().decrement(key, ticketCount.longValue());
        // Rollback if accidentally oversold
        if (remainingStock != null && remainingStock < 0) {
            redisTemplate.opsForValue().increment(key, ticketCount.longValue());
            return false;
        }
        return true;
    }
}
