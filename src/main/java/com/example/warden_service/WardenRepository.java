package com.example.warden_service;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WardenRepository extends JpaRepository<Warden, Long> {
    long countByStatus(String status);
}