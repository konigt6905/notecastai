package com.notecastai.note.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExportFormat {

    MD("md", "text/markdown"),
    TXT("txt", "text/plain"),
    HTML("html", "text/html"),
    PDF("pdf", "application/pdf"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final String extension;
    private final String contentType;

    public static ExportFormat fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Export format cannot be null");
        }

        String upperValue = value.toUpperCase();
        try {
            return ExportFormat.valueOf(upperValue);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Unsupported export format: " + value + ". Supported formats: MD, TXT, HTML, PDF, DOCX"
            );
        }
    }
}
