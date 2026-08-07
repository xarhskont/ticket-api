package com.kontaxakis.ticket_api.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.kontaxakis.ticket_api.entity.Order;
import com.kontaxakis.ticket_api.config.RabbitMQConfig;
import com.kontaxakis.ticket_api.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final OrderRepository orderRepository;

    /**
     * Listens to the RabbitMQ queue and saves incoming orders to the database.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveOrder(Order order) {
        orderRepository.save(order);
    }

}
