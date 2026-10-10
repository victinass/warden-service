
package com.example.warden_service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class WardenService {

    private static final Logger logger = LoggerFactory.getLogger(WardenService.class);

    private final WardenRepository wardenRepository;
    private final RestTemplate restTemplate;
    private final String reportServiceUrl;
    private final String fileStorageUrl;

    public WardenService(
            WardenRepository wardenRepository,
            RestTemplate restTemplate,
            @Value("${report-service.url}") String reportServiceUrl,
            @Value("${file-storage.url}") String fileStorageUrl) {
        this.wardenRepository = wardenRepository;
        this.restTemplate = restTemplate;
        this.reportServiceUrl = reportServiceUrl;
        this.fileStorageUrl = fileStorageUrl;
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

        ByteArrayResource resource = new ByteArrayResource(csv.toString().getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "wardens-report.csv";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                fileStorageUrl + "/api/files", new HttpEntity<>(body, headers), Map.class);
        return response.getBody() == null ? "Report saved" : response.getBody().toString();
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