package com.kontaxakis.ticket_api.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kontaxakis.ticket_api.entity.Event;
import com.kontaxakis.ticket_api.entity.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long> {
    public Optional<Event> findByTitle(String title);

    public List<Event> findByEventDate(Instant eventDate);

    public List<Event> findByStatus(EventStatus status);
}
