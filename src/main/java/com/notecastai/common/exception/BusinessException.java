package com.notecastai.common.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    private final String customCode;

    @Getter
    public enum BusinessCode {
        INTERNAL_ERROR("Internal server error"),
        INVALID_REQUEST("Invalid request"),
        CLERK_USER_ID_MISSING("Missing authenticated Clerk user id"),
        ENTITY_NOT_FOUND("Entity not found"),
        VALIDATION_FAILED("Validation failed"),
        TAG_MUST_NOT_BE_BLANK("Validation failed, tag name must not be blank."),
        LIMIT_OF_TAGS_REACHED("Validation failed, tag limit reached."),
        TAG_ALREADY_EXIST("Validation failed, tag already exist."),
        RESOURCE_NOT_FOUND("Resource not found"),
        CONFLICT("Conflict "),
        FORBIDDEN("Forbidden "),
        FEATURE_NOT_IMPLEMENTED("Feature not implemented"),
        LIMIT_EXCEEDED("Maximum limit exceeded"),
        AI_RESPONSE_PARSE_ERROR("Failed to parse AI response");

        private final String defaultMessage;

        BusinessCode(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public BusinessDetail append(String detail) {
            return new BusinessDetail(this, detail);
        }
    }

    public record BusinessDetail(BusinessCode code, String detail) {
        public BusinessDetail append(String extra) {
            return new BusinessDetail(code, detail + extra);
        }

        public String message() {
            return code.defaultMessage + detail;
        }
    }

    private BusinessException(String customCode, String message, Throwable cause) {
        super(message, cause);
        this.customCode = customCode;
    }

    public static BusinessException of(BusinessCode code) {
        return new BusinessException(code.name(), code.getDefaultMessage(), null);
    }

    public static BusinessException of(BusinessCode code, Throwable cause) {
        return new BusinessException(code.name(), code.getDefaultMessage(), cause);
    }

    public static BusinessException of(BusinessDetail detail) {
        return new BusinessException(detail.code().name(), detail.message(), null);
    }

    public static BusinessException of(BusinessDetail detail, Throwable cause) {
        return new BusinessException(detail.code().name(), detail.message(), cause);
    }
}