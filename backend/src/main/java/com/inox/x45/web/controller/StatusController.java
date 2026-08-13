package com.inox.x45.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/status")
public class StatusController {

    @GetMapping
    public Map<String, Object> status() {
        return Map.of(
            "application", "45x-portal-backend",
            "status", "UP",
            "timestamp", Instant.now().toString()
        );
    }
}
