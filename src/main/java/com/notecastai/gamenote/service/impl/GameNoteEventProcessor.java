package com.notecastai.gamenote.service.impl;

import com.notecastai.gamenote.domain.GameNoteStatus;
import com.notecastai.gamenote.domain.event.GameNoteCreatedEvent;
import com.notecastai.gamenote.service.GameNoteService;
import com.notecastai.integration.ai.NoteAiEditor;
import com.notecastai.integration.ai.dto.GameNoteAiRequest;
import com.notecastai.integration.ai.dto.GameNoteAiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameNoteEventProcessor {

    private final GameNoteService gameNoteService;
    private final NoteAiEditor noteAiEditor;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameNoteCreated(GameNoteCreatedEvent event) {
        Long gameNoteId = event.getGameNoteId();
        log.info("Processing GameNoteCreatedEvent: id={}", gameNoteId);

        try {
            // Update status to PROCESSING
            gameNoteService.updateStatus(gameNoteId, GameNoteStatus.PROCESSING);
            // Build AI request using service
            GameNoteAiRequest aiRequest = gameNoteService.buildAiRequest(gameNoteId);

            // Call AI to generate questions (with retry logic built-in)
            GameNoteAiResponse aiResponse = noteAiEditor.generateGameQuestions(aiRequest);

            // Update GameNote with generated questions
            gameNoteService.updateWithQuestions(gameNoteId, aiResponse.getQuestions());

            log.info("GameNote processing completed successfully: id={}, questionsGenerated={}",
                    gameNoteId, aiResponse.getQuestions().size());

        } catch (Exception e) {
            log.error("GameNote processing failed: id={}, error={}", gameNoteId, e.getMessage(), e);
            gameNoteService.updateWithError(gameNoteId, e.getMessage());
        }
    }
}
