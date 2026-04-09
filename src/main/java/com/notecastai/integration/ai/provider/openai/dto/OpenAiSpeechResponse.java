package com.notecastai.integration.ai.provider.openai.dto;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.MediaType;

@Value
@Builder
public class OpenAiSpeechResponse {
    byte[] audio;
    MediaType mediaType;
    String requestId;
}
