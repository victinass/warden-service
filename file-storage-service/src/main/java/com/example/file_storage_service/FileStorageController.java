package com.example.file_storage_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileStorageController {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageController.class);

    private final Path storagePath;

    public FileStorageController(@Value("${file-storage.path}") String storagePath) throws IOException {
        this.storagePath = Path.of(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(this.storagePath);
    }

    @PostMapping
    public Map<String, String> store(@RequestParam("file") MultipartFile file) {
        logger.info("File upload received: name={}, size={}", file.getOriginalFilename(), file.getSize());
        if (file.isEmpty()) {
            logger.warn("File upload rejected because the file is empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must not be empty");
        }

        String originalName = file.getOriginalFilename();
        String safeName = originalName == null ? "report-" + UUID.randomUUID() + ".csv"
                : Path.of(originalName).getFileName().toString();
        if (safeName.isBlank() || safeName.equals(".") || safeName.equals("..")) {
            logger.warn("File upload rejected because the file name is invalid: {}", originalName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        Path target = storagePath.resolve(safeName).normalize();
        if (!target.startsWith(storagePath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            logger.error("File upload failed: name={}, path={}", safeName, target, exception);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store file", exception);
        }
        logger.info("File saved: name={}, path={}, size={}", safeName, target, file.getSize());
        return Map.of("fileName", safeName, "path", target.toString(), "size", String.valueOf(file.getSize()));
    }
}
