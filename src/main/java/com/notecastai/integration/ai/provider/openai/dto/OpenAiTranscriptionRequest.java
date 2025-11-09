package com.notecastai.integration.ai.provider.openai.dto;

import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class OpenAiTranscriptionRequest {

    private final InputStream audioStream;
    private final String filename;
    private final String contentType;
    private final OpenAiTranscriptionModel model;
    private final TranscriptionLanguage language;

    @Builder.Default
    private final String responseFormat = "verbose_json";

    @Builder.Default
    private final List<String> timestampGranularities = List.of("word", "segment");

    @Builder.Default
    private final Double temperature = 0.0;

    public MultiValueMap<String, Object> toMultipartBody() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(
                contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE));
        fileHeaders.setContentDispositionFormData("file", filename != null ? filename : "audio.mp3");
        HttpEntity<InputStreamResource> fileEntity = new HttpEntity<>(
                new InputStreamResource(audioStream),
                fileHeaders
        );
        body.add("file", fileEntity);

        body.add("model", model.getModelId());

        if (language != null && language != TranscriptionLanguage.AUTO) {
            body.add("language", language.getCode());
        }

        body.add("response_format", responseFormat);
        body.add("temperature", String.valueOf(temperature));

        if (timestampGranularities != null) {
            timestampGranularities.forEach(granularity ->
                    body.add("timestamp_granularities[]", granularity));
        }

        return body;
    }
}
