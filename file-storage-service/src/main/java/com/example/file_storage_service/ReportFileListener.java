package com.example.file_storage_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Component
public class ReportFileListener {

    private static final Logger logger = LoggerFactory.getLogger(ReportFileListener.class);

    private final ObjectMapper objectMapper;
    private final Path storagePath;

    public ReportFileListener(
            ObjectMapper objectMapper,
            @Value("${file-storage.path}") String storagePath) throws IOException {
        this.objectMapper = objectMapper;
        this.storagePath = Path.of(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(this.storagePath);
    }

    @RabbitListener(queues = "${report.queue.name}")
    public void storeReport(String payload) {
        ReportFileMessage message;
        try {
            message = objectMapper.readValue(payload, ReportFileMessage.class);
        } catch (JsonProcessingException exception) {
            logger.error("Report message rejected because it is invalid JSON", exception);
            throw new AmqpRejectAndDontRequeueException("Invalid report message", exception);
        }

        String traceId = message.traceId();
        if (traceId != null && !traceId.isBlank()) {
            MDC.put(TraceIdFilter.TRACE_ID, traceId);
        }
        try {
            String safeName = safeFileName(message.fileName());
            Path target = storagePath.resolve(safeName).normalize();
            if (!target.startsWith(storagePath)) {
                throw new AmqpRejectAndDontRequeueException("Invalid report file name");
            }

            try {
                Files.writeString(
                        target,
                        message.content() == null ? "" : message.content(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException exception) {
                logger.error("Unable to save queued report: name={}, path={}", safeName, target, exception);
                throw new IllegalStateException("Unable to store queued report", exception);
            }
            logger.info("Queued report saved: name={}, path={}", safeName, target);
        } finally {
            if (traceId != null && !traceId.isBlank()) {
                MDC.remove(TraceIdFilter.TRACE_ID);
            }
        }
    }

    private String safeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "report-" + UUID.randomUUID() + ".csv";
        }
        String safeName = Path.of(fileName).getFileName().toString();
        if (safeName.equals(".") || safeName.equals("..")) {
            throw new AmqpRejectAndDontRequeueException("Invalid report file name");
        }
        return safeName;
    }
}
