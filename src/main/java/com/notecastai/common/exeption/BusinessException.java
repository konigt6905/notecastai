package com.notecastai.common.exeption;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    private String customCode;

    @Getter
    public enum BusinessCode {
        ENTITY_NOT_FOUND("Entity not found"),
        VALIDATION_FAILED("Validation failed"),
        TAG_MUST_NOT_BE_BLANK("Validation failed, tag name must not be blank."),
        LIMIT_OF_TAGS_REECHOED("Validation failed, tag limit reached."),
        TAG_ALREADY_EXIST("Validation failed, tag already exist."),
        RESOURCE_NOT_FOUND("Resource not found"),
        CONFLICT("Conflict "),
        FORBIDDEN("Forbidden ");

        private final String defaultMessage;
        private String appender = "";

        BusinessCode(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public BusinessCode append(String appender) {
            this.appender = appender;
            return this;
        }
    }

    private BusinessException(BusinessCode code, Throwable cause) {
        super(code.defaultMessage + code.getAppender(), cause);
        this.customCode = code.name();
    }

    public static BusinessException of(BusinessCode code, Throwable cause) {
        return new BusinessException(code, cause);
    }

    public static BusinessException of(BusinessCode code) {
        return new BusinessException(code, null);
    }
}