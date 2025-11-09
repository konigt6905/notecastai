package com.notecastai.integration.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class TextToSpeechResult {

    byte[] audioBytes;
    String mediaType;
    String fileExtension;

    /**
     * Optional provider specific information for observability.
     */
    String provider;
    String model;
    String voice;
    Long processingTimeMs;
    Long sizeBytes;
    Double estimatedDurationSeconds;
    String requestId;
}
