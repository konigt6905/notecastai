package com.notecastai.integration.ai.provider.openai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notecastai.common.exeption.TechnicalException;
import com.notecastai.integration.ai.provider.openai.dto.OpenAiSpeechRequest;
import com.notecastai.integration.ai.provider.openai.dto.OpenAiSpeechResponse;
import com.notecastai.integration.ai.provider.openai.dto.OpenAiTranscriptionRequest;
import com.notecastai.integration.ai.provider.openai.dto.OpenAiTranscriptionResponse;
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
public class OpenAiClient {

    private final RestClient openAiRestClient;
    private final ObjectMapper objectMapper;

    public OpenAiTranscriptionResponse transcribe(OpenAiTranscriptionRequest request) {
        try {
            log.debug("Calling OpenAI transcription API with model: {}", request.getModel().getModelId());

            ResponseEntity<String> responseEntity = openAiRestClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(request.toMultipartBody())
                    .retrieve()
                    .toEntity(String.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                throw TechnicalException.of(AI_SERVICE_ERROR)
                        .with("provider", "OpenAI")
                        .with("error", "Empty or error response")
                        .build();
            }

            OpenAiTranscriptionResponse response = objectMapper.readValue(responseEntity.getBody(), OpenAiTranscriptionResponse.class);
            response.setRequestId(responseEntity.getHeaders().getFirst("x-request-id"));
            return response;

        } catch (RestClientException e) {
            log.error("OpenAI transcription call failed: {}", e.getMessage(), e);
            throw TechnicalException.of(AI_SERVICE_ERROR)
                    .with("provider", "OpenAI")
                    .with("error", e.getMessage())
                    .cause(e)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse transcription response: {}", e.getMessage(), e);
            throw TechnicalException.of(AI_SERVICE_ERROR)
                    .with("provider", "OpenAI")
                    .with("error", "Failed to process transcription response: " + e.getMessage())
                    .cause(e)
                    .build();
        }
    }

    public OpenAiSpeechResponse createSpeech(OpenAiSpeechRequest request, MediaType acceptType) {
        try {
            log.debug("Calling OpenAI speech API with model: {}", request.getModel());

            RestClient.RequestHeadersSpec<?> spec = openAiRestClient.post()
                    .uri("/audio/speech")
                    .body(request);

            if (acceptType != null) {
                spec = spec.accept(acceptType);
            }

            ResponseEntity<byte[]> responseEntity = spec.retrieve().toEntity(byte[].class);

            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                throw TechnicalException.of(AI_SERVICE_ERROR)
                        .with("provider", "OpenAI")
                        .with("error", "Empty audio data")
                        .build();
            }

            MediaType responseMediaType = responseEntity.getHeaders().getContentType();
            if (responseMediaType == null) {
                responseMediaType = acceptType;
            }

            return OpenAiSpeechResponse.builder()
                    .audio(responseEntity.getBody())
                    .mediaType(responseMediaType)
                    .requestId(responseEntity.getHeaders().getFirst("x-request-id"))
                    .build();

        } catch (RestClientException e) {
            log.error("OpenAI speech call failed: {}", e.getMessage(), e);
            throw TechnicalException.of(AI_SERVICE_ERROR)
                    .with("provider", "OpenAI")
                    .with("error", e.getMessage())
                    .cause(e)
                    .build();
        }
    }
}
