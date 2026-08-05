package com.kontaxakis.ticket_api.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kontaxakis.ticket_api.entity.Event;
import com.kontaxakis.ticket_api.entity.Order;
import com.kontaxakis.ticket_api.entity.OrderStatus;
import com.kontaxakis.ticket_api.entity.User;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    public List<Order> findByUser(User user);

    public List<Order> findByEvent(Event event);

    public List<Order> findByStatus(OrderStatus status);

}
