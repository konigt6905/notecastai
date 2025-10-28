package com.notecastai.common.exeption;

import lombok.Getter;

import java.util.List;

@Getter
public class AiValidationException extends RuntimeException {

    private final List<String> validationErrors;
    private final String rawContent;

    public AiValidationException(String message, List<String> validationErrors, String rawContent) {
        super(message + ": " + String.join(", ", validationErrors));
        this.validationErrors = validationErrors;
        this.rawContent = rawContent;
    }

    public AiValidationException(String message, String rawContent) {
        super(message);
        this.validationErrors = List.of(message);
        this.rawContent = rawContent;
    }
}