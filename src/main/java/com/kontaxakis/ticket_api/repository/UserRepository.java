package com.kontaxakis.ticket_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kontaxakis.ticket_api.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByUsername(String username);

    public Optional<User> findByEmail(String email);
}