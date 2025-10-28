package com.notecastai.integration.ai.provider.groq.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranscriptionMetadata {
    private String modelUsed;
    private String provider;
    private String requestId;
    private Long processingTimeMs;
}
