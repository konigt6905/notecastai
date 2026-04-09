package com.notecastai.integration.ai.provider.openrouter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpenRouterResponse {

    private String content;
    private String model;
    private String finishReason;
    private TokenUsage tokenUsage;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenUsage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
