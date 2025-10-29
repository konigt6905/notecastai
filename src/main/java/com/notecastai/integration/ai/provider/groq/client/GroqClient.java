package com.notecastai.integration.ai.provider.groq.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notecastai.common.exeption.TechnicalException;
import com.notecastai.integration.ai.provider.groq.dto.GroqTranscriptionRequest;
import com.notecastai.integration.ai.provider.groq.dto.GroqTranscriptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static com.notecastai.common.exeption.TechnicalException.Code.AI_SERVICE_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroqClient {

    private final RestClient groqRestClient;
    private final ObjectMapper objectMapper;

    public GroqTranscriptionResponse transcribe(GroqTranscriptionRequest request) {
        try {
            log.debug("Calling Groq transcription API with model: {}", request.getModel().getModelId());

            ResponseEntity<String> responseEntity = groqRestClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(request.toMultipartBody())
                    .retrieve()
                    .toEntity(String.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                throw TechnicalException.of(AI_SERVICE_ERROR)
                        .with("provider", "Groq")
                        .with("error", "Empty or error response")
                        .build();
            }

            return objectMapper.readValue(responseEntity.getBody(), GroqTranscriptionResponse.class);
        } catch (RestClientException e) {
            log.error("Groq API call failed: {}", e.getMessage(), e);
            throw TechnicalException.of(AI_SERVICE_ERROR)
                    .with("provider", "Groq")
                    .with("error", e.getMessage())
                    .cause(e)
                    .build();
        } catch (Exception e) {
            log.error("Transcription processing failed: {}", e.getMessage(), e);
            throw TechnicalException.of(AI_SERVICE_ERROR)
                    .with("provider", "Groq")
                    .with("error", "Failed to process transcription: " + e.getMessage())
                    .cause(e)
                    .build();
        }
    }
}
