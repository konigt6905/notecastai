package com.notecastai.integration.ai.provider.openai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiSpeechRequest {

    String model;
    String input;
    String voice;

    @JsonProperty("response_format")
    String responseFormat;

    Double speed;
}
