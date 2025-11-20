package com.notecastai.gamenote.service.impl;

import com.notecastai.gamenote.api.dto.GameQuestionDTO;
import com.notecastai.gamenote.domain.GameNoteEntity;
import com.notecastai.gamenote.domain.GameNoteStatus;
import com.notecastai.gamenote.domain.GameQuestionEntity;
import com.notecastai.gamenote.domain.event.GameNoteCreatedEvent;
import com.notecastai.integration.ai.dto.GameNoteAiRequest;
import com.notecastai.integration.ai.dto.GameNoteAiResponse;
import com.notecastai.gamenote.infrastructure.repo.GameNoteRepository;
import com.notecastai.gamenote.service.GameNoteService;
import com.notecastai.integration.ai.NoteAiEditor;
import com.notecastai.note.domain.NoteEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameNoteEventProcessor {

    private final GameNoteRepository gameNoteRepository;
    private final GameNoteService gameNoteService;
    private final NoteAiEditor noteAiEditor;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameNoteCreated(GameNoteCreatedEvent event) {
        Long gameNoteId = event.getGameNoteId();
        log.info("Processing GameNoteCreatedEvent: id={}", gameNoteId);

        try {
            // Load entity
            GameNoteEntity entity = gameNoteRepository.getOrThrow(gameNoteId);

            // Update status to PROCESSING
            entity.setStatus(GameNoteStatus.PROCESSING);
            gameNoteRepository.save(entity);

            // Prepare AI request
            NoteEntity sourceNote = entity.getSourceNote();
            GameNoteAiRequest aiRequest = GameNoteAiRequest.builder()
                    .noteTitle(sourceNote.getTitle())
                    .noteContent(getNoteContent(sourceNote))
                    .numberOfQuestions(entity.getNumberOfQuestions())
                    .questionLength(entity.getQuestionLength())
                    .answerLength(entity.getAnswerLength())
                    .difficulty(entity.getDifficulty())
                    .questionType(entity.getQuestionType())
                    .customInstructions(entity.getCustomInstructions())
                    .build();

            // Call AI to generate questions (with retry logic built-in)
            GameNoteAiResponse aiResponse = noteAiEditor.generateGameQuestions(aiRequest);

            // Convert AI response to question entities
            entity = gameNoteRepository.getOrThrow(gameNoteId);
            entity.getQuestions().clear(); // Clear any existing questions

            int order = 0;
            for (GameQuestionDTO questionDto : aiResponse.getQuestions()) {
                GameQuestionEntity questionEntity = GameQuestionEntity.builder()
                        .gameNote(entity)
                        .questionOrder(order++)
                        .type(questionDto.getType())
                        .questionText(questionDto.getQuestionText())
                        .options(questionDto.getOptions() != null ? new ArrayList<>(questionDto.getOptions()) : new ArrayList<>())
                        .correctAnswer(questionDto.getCorrectAnswer())
                        .answer(questionDto.getAnswer())
                        .explanation(questionDto.getExplanation())
                        .hint(questionDto.getHint())
                        .build();

                entity.addQuestion(questionEntity);
            }

            entity.setStatus(GameNoteStatus.PROCESSED);
            entity.setErrorMessage(null);
            gameNoteRepository.save(entity);

            log.info("GameNote processing completed successfully: id={}, questionsGenerated={}",
                    gameNoteId, aiResponse.getQuestions().size());

        } catch (Exception e) {
            log.error("GameNote processing failed: id={}, error={}", gameNoteId, e.getMessage(), e);
            gameNoteService.updateWithError(gameNoteId, e.getMessage());
        }
    }

    private String getNoteContent(NoteEntity sourceNote) {
        // Prefer knowledge base, fallback to formatted note
        String content = sourceNote.getKnowledgeBase();
        if (content == null || content.isBlank()) {
            content = sourceNote.getFormattedNote();
        }
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Source note has no content");
        }
        return content;
    }
}
