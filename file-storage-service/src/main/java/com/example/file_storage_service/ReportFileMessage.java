package com.example.file_storage_service;

public record ReportFileMessage(String fileName, String content, String traceId) {
}
