package com.notecastai.common.exeption;

import lombok.Getter;

import java.util.List;

@Getter
public class AiValidationException extends RuntimeException {

    private final List<String> validationErrors;
    private final String rawContent;
    private final Code code;

    public enum Code {
        DEFAULT("Error during repose validation"),
        TOO_LONG_CONTENT("Provided content is too long") ;
        private final String template;
        Code(String template) { this.template = template; }
        String template() { return template; }
    }

    public AiValidationException(String message, List<String> validationErrors, String rawContent) {
        super(message + ": " + String.join(", ", validationErrors));
        this.validationErrors = validationErrors;
        this.rawContent = rawContent;
        this.code = Code.DEFAULT;
    }

    public AiValidationException(String message, Code code, String rawContent) {
        super(message);
        this.validationErrors = List.of(message);
        this.code = code;
        this.rawContent = rawContent;
    }

    public AiValidationException(String message, String rawContent) {
        super(message);
        this.validationErrors = List.of(message);
        this.rawContent = rawContent;
        this.code = Code.DEFAULT;
    }
}