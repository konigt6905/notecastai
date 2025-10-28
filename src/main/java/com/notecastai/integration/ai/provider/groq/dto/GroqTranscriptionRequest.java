package com.notecastai.integration.ai.provider.groq.dto;

import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


import java.io.InputStream;
import java.util.List;

@Getter
@Builder
public class GroqTranscriptionRequest {

    private final InputStream audioStream;
    private final String contentType;
    private final GroqWhisperModel model;
    private final TranscriptionLanguage language;

    @Builder.Default
    private final String responseFormat = "verbose_json";

    @Builder.Default
    private final List<String> timestampGranularities = List.of("word", "segment");

    @Builder.Default
    private final Double temperature = 0.0;

    /**
     * Converts this request object to a Spring MultiValueMap for multipart/form-data submission
     */
    public MultiValueMap<String, Object> toMultipartBody() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Add audio file
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(contentType));
        HttpEntity<InputStreamResource> fileEntity = new HttpEntity<>(
                new InputStreamResource(audioStream),
                fileHeaders
        );
        body.add("file", fileEntity);

        // Add model
        body.add("model", model.getModelId());

        // Add language if not auto-detect
        if (language != TranscriptionLanguage.AUTO) {
            body.add("language", language.getCode());
        }

        // Add response format
        body.add("response_format", responseFormat);

        // Add timestamp granularities
        if (timestampGranularities != null) {
            timestampGranularities.forEach(granularity ->
                    body.add("timestamp_granularities[]", granularity)
            );
        }

        // Add temperature
        body.add("temperature", String.valueOf(temperature));

        return body;
    }
}
