package com.notecastai.notecast.event;

import com.notecastai.common.exeption.AiValidationException;
import com.notecastai.integration.ai.NoteCastTranscriptGenerator;
import com.notecastai.integration.ai.TextToSpeechService;
import com.notecastai.integration.ai.dto.TextToSpeechRequest;
import com.notecastai.integration.ai.dto.TextToSpeechResult;
import com.notecastai.integration.storage.StorageService;
import com.notecastai.notecast.domain.NoteCastStatus;
import com.notecastai.notecast.event.dto.NoteCastCreatedEvent;
import com.notecastai.notecast.service.NoteCastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.ByteArrayInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoteCastEventListener {

    private final NoteCastTranscriptGenerator transcriptGenerator;
    private final NoteCastService noteCastService;
    private final TextToSpeechService textToSpeechService;
    private final StorageService storageService;

    private static final String NOTECAST_AUDIO_KEY_TEMPLATE = "note-casts/%d/audio%s";

    @Async("noteCastProcessingExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNoteCastCreated(NoteCastCreatedEvent event) {
        Long noteCastId = event.getNoteCastId();
        log.info("Processing NoteCast creation event for ID: {} - style: {}, size: {}",
                noteCastId, event.getStyle().getLabel(), event.getSize().getLabel());

        try {
            // Update status to processing
            noteCastService.updateStatus(noteCastId, NoteCastStatus.PROCESSING_TRANSCRIPT);

            // Generate transcript with retry logic
            String transcript = transcriptGenerator.generateTranscript(
                    event.getNoteContent(),
                    event.getStyle(),
                    event.getSize()
            );

            // Update with transcript and set status to WAITING_FOR_TTS
            noteCastService.updateWithTranscript(noteCastId, transcript);

            log.info("NoteCast transcript generation completed successfully for ID: {}, transcript length: {} chars, style: {}, size: {}",
                    noteCastId, transcript.length(), event.getStyle().getLabel(), event.getSize().getLabel());

            noteCastService.updateStatus(noteCastId, NoteCastStatus.PROCESSING_TTS);

            TextToSpeechRequest ttsRequest = TextToSpeechRequest.builder()
                    .referenceId(noteCastId)
                    .transcript(transcript)
                    .voice(event.getVoice())
                    .style(event.getStyle())
                    .size(event.getSize())
                    .build();

            TextToSpeechResult speechResult = textToSpeechService.synthesizeSpeech(ttsRequest);
            String audioKey = uploadAudio(noteCastId, speechResult);

            Integer durationSeconds = speechResult.getEstimatedDurationSeconds() != null
                    ? speechResult.getEstimatedDurationSeconds().intValue()
                    : null;

            noteCastService.updateWithAudio(
                    noteCastId,
                    audioKey,
                    durationSeconds,
                    speechResult.getProcessingTimeMs()
            );

            log.info("NoteCast TTS completed successfully for ID: {}, audioKey: {}, duration: {}s",
                    noteCastId,
                    audioKey,
                    durationSeconds);

        } catch (AiValidationException e) {
            log.error("Transcript validation failed for NoteCast ID: {}. Errors: {}",
                    noteCastId, e.getValidationErrors(), e);
            noteCastService.updateWithError(noteCastId,
                    "Transcript validation failed: " + String.join(", ", e.getValidationErrors()));

        } catch (IllegalArgumentException e) {
            log.error("Invalid input for NoteCast ID: {}", noteCastId, e);
            noteCastService.updateWithError(noteCastId, "Invalid input: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error processing NoteCast creation event for ID: {}", noteCastId, e);
            noteCastService.updateWithError(noteCastId,
                    "NoteCast processing failed: " + e.getMessage());
        }
    }

    private String uploadAudio(Long noteCastId, TextToSpeechResult result) {
        byte[] audioBytes = result.getAudioBytes();
        if (audioBytes == null || audioBytes.length == 0) {
            throw new IllegalStateException("Text-to-speech service returned empty audio payload");
        }

        String extension = result.getFileExtension() != null ? result.getFileExtension() : ".mp3";
        String key = String.format(NOTECAST_AUDIO_KEY_TEMPLATE, noteCastId, extension);
        String contentType = result.getMediaType() != null ? result.getMediaType() : "audio/mpeg";

        log.info("Uploading NoteCast audio to S3: key={}, size={} bytes", key, audioBytes.length);
        return storageService.put(
                key,
                new ByteArrayInputStream(audioBytes),
                audioBytes.length,
                contentType
        );
    }

}