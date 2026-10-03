package com.example.report_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final WardenRepository wardenRepository;

    public ReportController(WardenRepository wardenRepository) {
        this.wardenRepository = wardenRepository;
    }

    @GetMapping("/wardens")
    public List<Warden> getWardens() {
        return wardenRepository.findAll();
    }
}