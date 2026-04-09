package com.notecastai.voicenote.service.impl;

import com.notecastai.integration.ai.TranscriptionService;
import com.notecastai.integration.ai.dto.TranscriptionResult;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.voicenote.api.mapper.VoiceNoteMapper;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import com.notecastai.voicenote.domain.VoiceNoteEntity;
import com.notecastai.voicenote.domain.VoiceNoteStatus;
import com.notecastai.voicenote.infrastructure.repo.VoiceNoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class VoiceNoteHelper {

    private final VoiceNoteRepository voiceNoteRepository;
    private final NoteRepository noteRepository;
    private final TimestampJsonMapper timestampJsonMapper;
    private final TranscriptionService transcriptionService;

    public VoiceNoteHelper(VoiceNoteRepository voiceNoteRepository,
                           NoteRepository noteRepository,
                           TimestampJsonMapper timestampJsonMapper,
                           @Qualifier("openAiAudioService") TranscriptionService transcriptionService) {
        this.voiceNoteRepository = voiceNoteRepository;
        this.noteRepository = noteRepository;
        this.timestampJsonMapper = timestampJsonMapper;
        this.transcriptionService = transcriptionService;
    }

    @Transactional
    public void updateStatus(Long voiceNoteId, VoiceNoteStatus status) {
        VoiceNoteEntity entity = voiceNoteRepository.getOrThrow(voiceNoteId);
        entity.setStatus(status);
        voiceNoteRepository.saveAndFlush(entity);
    }

    @Async("voiceNoteProcessingExecutor")
    public CompletableFuture<TranscriptionResult> transcribeAsync(
            InputStream audioStream,
            String filename,
            String contentType,
            TranscriptionLanguage preferredLanguage) {
        log.info("Starting async transcription for file: {}", filename);
        return transcriptionService.transcribeAudioFile(audioStream, filename, contentType, preferredLanguage)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Transcription failed for file: {}", filename, error);
                    } else {
                        log.info("Transcription completed for file: {}", filename);
                    }
                });
    }

    @Transactional
    public void saveTranscriptionResult(
            Long voiceNoteId,
            String s3FileUrl,
            TranscriptionResult result,
            String title,
            Long noteId
    ) {
        VoiceNoteEntity voiceNote = voiceNoteRepository.getOrThrow(voiceNoteId);

        voiceNote.setNote(noteRepository.getOrThrow(noteId));
        voiceNote.setTitle(title);
        voiceNote.setTranscript(result.getTranscript());
        voiceNote.setLanguage(TranscriptionLanguage.fromCode(result.getLanguage()));
        voiceNote.setDurationSeconds(result.getDurationSeconds());
        voiceNote.setS3FileUrl(s3FileUrl);

        if (result.getWordTimestamps() != null && !result.getWordTimestamps().isEmpty()) {
            String wordTimestampsJson = timestampJsonMapper.serializeWordTimestamps(
                    result.getWordTimestamps()
            );
            voiceNote.setWordTimestampsJson(wordTimestampsJson);
        }

        if (result.getSegmentTimestamps() != null && !result.getSegmentTimestamps().isEmpty()) {
            String segmentTimestampsJson = timestampJsonMapper.serializeSegmentTimestamps(
                    result.getSegmentTimestamps()
            );
            voiceNote.setSegmentTimestampsJson(segmentTimestampsJson);
        }

        if (result.getMetadata() != null) {
            voiceNote.setTranscriptProcessingTimeMs(result.getMetadata().getProcessingTimeMs());
        }

        voiceNote.setStatus(VoiceNoteStatus.PROCESSED);
        voiceNoteRepository.saveAndFlush(voiceNote);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateWithError(Long voiceNoteId, String errorMessage) {
        VoiceNoteEntity entity = voiceNoteRepository.getOrThrow(voiceNoteId);
        entity.markAsFailed(errorMessage);
        voiceNoteRepository.saveAndFlush(entity);
    }

}
