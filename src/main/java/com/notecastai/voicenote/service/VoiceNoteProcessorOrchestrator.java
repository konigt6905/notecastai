package com.notecastai.voicenote.service;

import com.notecastai.integration.ai.dto.TranscriptionResult;
import com.notecastai.integration.storage.StorageService;
import com.notecastai.note.api.dto.NoteDTO;
import com.notecastai.note.service.command.CreateNoteCommand;
import com.notecastai.note.domain.NoteType;
import com.notecastai.note.service.NoteService;
import com.notecastai.voicenote.domain.event.VoiceNoteCreatedEvent;
import com.notecastai.voicenote.service.impl.VoiceNoteHelper;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.ByteArrayInputStream;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceNoteProcessorOrchestrator {

    private final StorageService storageService;
    private final VoiceNoteHelper voiceNoteHelper;
    private final NoteService noteService;
    private final Retry voiceNoteProcessingRetry;

    @Async("voiceNoteProcessingExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVoiceNoteCreated(VoiceNoteCreatedEvent event) {
        Long voiceNoteId = event.getVoiceNoteId();
        log.info("Processing VoiceNoteCreatedEvent: id={}", voiceNoteId);

        try {
            voiceNoteHelper.updateStatus(voiceNoteId,
                    com.notecastai.voicenote.domain.VoiceNoteStatus.PROCESSING);

            String s3Key = buildS3Key(voiceNoteId, event.getOriginalFilename());

            CompletableFuture<String> uploadFut = storageService.putAsync(
                    s3Key,
                    new ByteArrayInputStream(event.getAudioBytes()),
                    event.getFileSize(),
                    event.getContentType()
            );

            CompletableFuture<TranscriptionResult> transFut = voiceNoteHelper.transcribeAsync(
                    new ByteArrayInputStream(event.getAudioBytes()),
                    event.getOriginalFilename(),
                    event.getContentType(),
                    event.getPreferredLanguage()
            );

            String s3Url = uploadFut.join();
            TranscriptionResult tr = transFut.join();

            CreateNoteCommand createNoteCommand = CreateNoteCommand.builder()
                    .title(event.getTitle())
                    .tagIds(event.getTagIds())
                    .type(NoteType.VOICENOTE)
                    .knowledgeBase(tr.getTranscript())
                    .formatType(event.getFormatType())
                    .adjustTagsWithAi(true)
                    .instructions(event.getUserInstructions())
                    .build();

            NoteDTO note = Retry.decorateSupplier(
                    voiceNoteProcessingRetry,
                    () -> noteService.create(createNoteCommand)
            ).get();

            String title = event.getTitle();
            if (title == null || title.isBlank()) {
                log.info("Title is null or blank, using AI-adjusted title: {}", note.getTitle());
                title = note.getTitle();
            }

            voiceNoteHelper.saveTranscriptionResult(voiceNoteId, s3Url, tr, title, note.getId());

            log.info("Voice note processing completed successfully: id={}", voiceNoteId);

        } catch (Exception e) {
            log.error("Voice note processing failed: id={}, error={}", voiceNoteId, e.getMessage(), e);
            voiceNoteHelper.updateWithError(voiceNoteId, "Processing failed: " + e.getMessage());
        }
    }

    private String buildS3Key(Long voiceNoteId, String filename) {
        return String.format("voice-notes/%d/%s", voiceNoteId, filename);
    }
}
