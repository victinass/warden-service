
package com.example.warden_service;

import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;

@Service
public class WardenService {

    private static final Logger logger = LoggerFactory.getLogger(WardenService.class);

    private final WardenRepository wardenRepository;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String reportServiceUrl;
    private final String reportQueueName;

    public WardenService(
            WardenRepository wardenRepository,
            RestTemplate restTemplate,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${report-service.url}") String reportServiceUrl,
            @Value("${report.queue.name}") String reportQueueName) {
        this.wardenRepository = wardenRepository;
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.reportServiceUrl = reportServiceUrl;
        this.reportQueueName = reportQueueName;
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

    public String saveReport() {
        List<Warden> wardens = getReportWardens();
        StringBuilder csv = new StringBuilder("id,lastName,firstName,patronymic,birthDate,position,hireDate,fireDate,status\n");
        for (Warden warden : wardens) {
            csv.append(warden.getId()).append(',')
                    .append(csvValue(warden.getLastName())).append(',')
                    .append(csvValue(warden.getFirstName())).append(',')
                    .append(csvValue(warden.getPatronymic())).append(',')
                    .append(warden.getBirthDate()).append(',')
                    .append(csvValue(warden.getPosition())).append(',')
                    .append(warden.getHireDate()).append(',')
                    .append(warden.getFireDate()).append(',')
                    .append(csvValue(warden.getStatus())).append('\n');
        }

        try {
            String message = objectMapper.writeValueAsString(
                    new ReportFileMessage(
                            "wardens-report.csv",
                            csv.toString(),
                            MDC.get(TraceIdFilter.TRACE_ID)));
            rabbitTemplate.convertAndSend(reportQueueName, message);
        } catch (JsonProcessingException exception) {
            logger.error("Unable to serialize report for queue", exception);
            throw new IllegalStateException("Unable to queue report", exception);
        }
        logger.info("Wardens report queued for file storage");
        return "Report queued";
    }

    private String csvValue(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return escaped.contains(",") || escaped.contains("\"")
                ? "\"" + escaped + "\""
                : escaped;
    }
}