package com.notecastai.integration.ai.provider.openrouter.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpenRouterResponse {

    private String content;
    private String model;
    private String finishReason;
    private TokenUsage tokenUsage;

    @Data
    @Builder
    public static class TokenUsage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
