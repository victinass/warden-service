package com.example.warden_service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WardenNotFoundException extends RuntimeException {

    public WardenNotFoundException(String message) {
        super(message);
    }
}