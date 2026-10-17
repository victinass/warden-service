package com.example.warden_service;

public record ReportFileMessage(String fileName, String content, String traceId) {
}
