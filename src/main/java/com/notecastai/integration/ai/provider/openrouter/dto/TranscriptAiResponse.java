package com.notecastai.integration.ai.provider.openrouter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptAiResponse {

    @JsonProperty("transcript")
    private String transcript;

    @JsonProperty("estimatedDuration")
    private String estimatedDuration;

    @JsonProperty("wordCount")
    private Integer wordCount;
}