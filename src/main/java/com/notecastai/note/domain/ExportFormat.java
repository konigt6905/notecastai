package com.notecastai.note.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExportFormat {

    /**
     * Markdown format - Plain text with markdown syntax
     */
    MD("md", "text/markdown"),

    /**
     * Plain text format - No formatting, just raw text
     */
    TXT("txt", "text/plain"),

    /**
     * HTML format - Formatted HTML document
     */
    HTML("html", "text/html"),

    /**
     * PDF format - Portable Document Format (not yet implemented)
     */
    PDF("pdf", "application/pdf"),

    /**
     * Microsoft Word format - DOCX document (not yet implemented)
     */
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    /** File extension for this format */
    private final String extension;

    /** MIME content type for HTTP response headers */
    private final String contentType;

    /**
     * Parse string value to enum, case-insensitive.
     *
     * @param value String representation of format (e.g., "md", "MD", "Md")
     * @return Corresponding ExportFormat enum
     * @throws IllegalArgumentException if format is not supported
     */
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
