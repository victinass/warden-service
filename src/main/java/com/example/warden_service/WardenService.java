
package com.example.warden_service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class WardenService {

    private static final Logger logger = LoggerFactory.getLogger(WardenService.class);

    private final WardenRepository wardenRepository;
    private final RestTemplate restTemplate;
    private final String reportServiceUrl;

    public WardenService(
            WardenRepository wardenRepository,
            RestTemplate restTemplate,
            @Value("${report-service.url}") String reportServiceUrl) {
        this.wardenRepository = wardenRepository;
        this.restTemplate = restTemplate;
        this.reportServiceUrl = reportServiceUrl;
    }

    public Warden create(Warden warden) {
        return wardenRepository.save(warden);
    }

    public List<Warden> getAll() {
        return wardenRepository.findAll();
    }

    public Warden getById(Long id) {
        return wardenRepository.findById(id)
                .orElseThrow(() -> new WardenNotFoundException("Warden not found"));
    }

    public Warden update(Long id, Warden updatedWarden) {
        Warden warden = getById(id);

        warden.setLastName(updatedWarden.getLastName());
        warden.setFirstName(updatedWarden.getFirstName());
        warden.setPatronymic(updatedWarden.getPatronymic());
        warden.setBirthDate(updatedWarden.getBirthDate());
        warden.setPosition(updatedWarden.getPosition());
        warden.setHireDate(updatedWarden.getHireDate());
        warden.setFireDate(updatedWarden.getFireDate());
        warden.setStatus(updatedWarden.getStatus());

        return wardenRepository.save(warden);
    }

    public Warden fire(Long id) {
        Warden warden = getById(id);

        warden.setStatus("FIRED");
        warden.setFireDate(java.time.LocalDate.now());

        return wardenRepository.save(warden);
    }

    public void delete(Long id) {
        Warden warden = getById(id);
        wardenRepository.delete(warden);
    }

    public long countWorking() {
    return wardenRepository.countByStatus("WORKING");
    }

    public long countFired() {
        return wardenRepository.countByStatus("FIRED");
    }

    public List<Warden> getReportWardens() {
        logger.info("Requesting wardens report from report-service");
        ResponseEntity<List<Warden>> response = restTemplate.exchange(
                reportServiceUrl + "/api/reports/wardens",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

            logger.info("Received wardens report from report-service");
        return response.getBody() == null ? List.of() : response.getBody();
    }
}