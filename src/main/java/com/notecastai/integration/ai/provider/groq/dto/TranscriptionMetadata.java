package com.notecastai.integration.ai.provider.groq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptionMetadata {
    private String modelUsed;
    private String provider;
    private String requestId;
    private Long processingTimeMs;
}
