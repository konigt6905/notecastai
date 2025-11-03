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
        INTERNAL_ERROR("Internal server error"),
        INVALID_REQUEST("Invalid request"),
        CLERK_USER_ID_MISSING("Missing authenticated Clerk user id"),
        ENTITY_NOT_FOUND("Entity not found"),
        VALIDATION_FAILED("Validation failed"),
        TAG_MUST_NOT_BE_BLANK("Validation failed, tag name must not be blank."),
        LIMIT_OF_TAGS_REECHOED("Validation failed, tag limit reached."),
        TAG_ALREADY_EXIST("Validation failed, tag already exist."),
        RESOURCE_NOT_FOUND("Resource not found"),
        CONFLICT("Conflict "),
        FORBIDDEN("Forbidden "),
        FEATURE_NOT_IMPLEMENTED("Feature not implemented");

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