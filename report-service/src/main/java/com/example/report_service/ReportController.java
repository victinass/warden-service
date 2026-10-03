package com.example.report_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final WardenRepository wardenRepository;

    public ReportController(WardenRepository wardenRepository) {
        this.wardenRepository = wardenRepository;
    }

    @GetMapping("/wardens")
    public List<Warden> getWardens() {
        logger.info("Preparing wardens report");
        return wardenRepository.findAll();
    }
}