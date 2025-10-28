package com.notecastai.notecast.event;

import com.notecastai.common.exeption.AiValidationException;
import com.notecastai.integration.ai.NoteCastTranscriptGenerator;
import com.notecastai.notecast.domain.NoteCastStatus;
import com.notecastai.notecast.event.dto.NoteCastCreatedEvent;
import com.notecastai.notecast.service.NoteCastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoteCastEventListener {

    private final NoteCastTranscriptGenerator transcriptGenerator;
    private final NoteCastService noteCastService;

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
                    "Transcript generation failed: " + e.getMessage());
        }
    }

}