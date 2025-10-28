package com.notecastai.integration.ai.provider.groq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notecastai.common.exeption.TechnicalException;
import com.notecastai.integration.ai.TranscriptionService;
import com.notecastai.integration.ai.provider.groq.dto.*;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.notecastai.common.exeption.TechnicalException.Code.AI_SERVICE_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroqAI implements TranscriptionService {

    private final RestClient groqRestClient;
    private final ObjectMapper objectMapper;
    private final Retry noteAiRetry;

    @Override
    public CompletableFuture<TranscriptionResult> transcribeAudioFile(
            InputStream audioStream,
            String filename,
            String contentType,
            TranscriptionLanguage language
    ) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Starting Groq transcription for file: {}, language: {}", filename, language.getCode());

            long startTime = System.currentTimeMillis();

            GroqWhisperModel model = GroqWhisperModel.WHISPER_LARGE_V3_TURBO;

            // Build request
            GroqTranscriptionRequest request = GroqTranscriptionRequest.builder()
                    .audioStream(audioStream)
                    .contentType(contentType)
                    .model(model)
                    .language(language)
                    .build();

            // Execute with retry
            GroqTranscriptionResponse response = Retry.decorateSupplier(noteAiRetry, () -> {
                try {
                    return callGroqTranscriptionApi(request);
                } catch (Exception e) {
                    log.error("Groq transcription failed: {}", e.getMessage(), e);
                    throw new RuntimeException("Transcription failed", e);
                }
            }).get();

            long processingTime = System.currentTimeMillis() - startTime;

            log.info("Groq transcription completed: {} words, {} segments, {} ms",
                    response.getWords() != null ? response.getWords().size() : 0,
                    response.getSegments() != null ? response.getSegments().size() : 0,
                    processingTime);

            return mapToTranscriptionResult(response, model, processingTime);
        });
    }

    private GroqTranscriptionResponse callGroqTranscriptionApi(GroqTranscriptionRequest request) {
        try {
            log.debug("Calling Groq transcription API with model: {}", request.getModel().getModelId());

            // Make request
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

    private TranscriptionResult mapToTranscriptionResult(
            GroqTranscriptionResponse response,
            GroqWhisperModel model,
            long processingTimeMs
    ) {
        // Map word timestamps
        List<WordTimestamp> wordTimestamps = null;
        if (response.getWords() != null) {
            wordTimestamps = response.getWords().stream()
                    .map(word -> WordTimestamp.builder()
                            .word(word.getWord())
                            .startTime(word.getStart())
                            .endTime(word.getEnd())
                            .build())
                    .collect(Collectors.toList());
        }

        // Map segment timestamps
        List<SegmentTimestamp> segmentTimestamps = null;
        if (response.getSegments() != null) {
            segmentTimestamps = response.getSegments().stream()
                    .map(segment -> SegmentTimestamp.builder()
                            .id(segment.getId())
                            .text(segment.getText())
                            .startTime(segment.getStart())
                            .endTime(segment.getEnd())
                            .averageLogProbability(segment.getAvgLogprob())
                            .compressionRatio(segment.getCompressionRatio())
                            .noSpeechProbability(segment.getNoSpeechProb())
                            .build())
                    .collect(Collectors.toList());
        }

        // Build metadata
        TranscriptionMetadata metadata = TranscriptionMetadata.builder()
                .modelUsed(model.getModelId())
                .provider("Groq")
                .requestId(response.getXGroq() != null ? response.getXGroq().getId() : null)
                .processingTimeMs(processingTimeMs)
                .build();

        // Calculate duration from segments or response
        Integer durationSeconds = null;
        if (response.getDuration() != null) {
            durationSeconds = response.getDuration().intValue();
        } else if (segmentTimestamps != null && !segmentTimestamps.isEmpty()) {
            SegmentTimestamp lastSegment = segmentTimestamps.get(segmentTimestamps.size() - 1);
            if (lastSegment.getEndTime() != null) {
                durationSeconds = lastSegment.getEndTime().intValue();
            }
        }

        return TranscriptionResult.builder()
                .transcript(response.getText())
                .language(response.getLanguage())
                .durationSeconds(durationSeconds)
                .wordTimestamps(wordTimestamps)
                .segmentTimestamps(segmentTimestamps)
                .metadata(metadata)
                .build();
    }
}