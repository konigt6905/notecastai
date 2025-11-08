package com.notecastai.common.exeption;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TechnicalException extends RuntimeException {

    public enum Code {
        AI_SERVICE_ERROR("Error during AI service call "),
        S3_ERROR("Exception during S3 service call "),
        EXTERNAL_SERVICE_ERROR("External service error: {service}"),
        INTERNAL_ERROR("Internal error");

        private final String template;
        Code(String template) { this.template = template; }
        String template() { return template; }
    }

    private final Code code;
    private final Map<String, Object> context;

    private TechnicalException(Code code, String message, Throwable cause, Map<String, Object> context) {
        super(message, cause);
        this.code = code;
        this.context = context == null ? Map.of() : Collections.unmodifiableMap(context);
    }

    public static Builder of(Code code) { return new Builder(code); }

    public static class Builder {
        private final Code code;
        private final Map<String, Object> ctx = new HashMap<>();
        private Throwable cause;

        private Builder(Code code) { this.code = code; }

        public Builder with(String key, Object value) { ctx.put(key, value); return this; }
        public Builder cause(Throwable t) { this.cause = t; return this; }

        public TechnicalException build() {
            String msg = render(code.template(), ctx);
            return new TechnicalException(code, msg, cause, Map.copyOf(ctx));
        }

        private static String render(String template, Map<String, Object> ctx) {
            String out = template;
            for (var e : ctx.entrySet()) {
                out = out.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
            }
            return out;
        }
    }
}