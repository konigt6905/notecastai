package com.notecastai.voicenote.service.impl;

import com.notecastai.integration.ai.TranscriptionService;
import com.notecastai.integration.ai.provider.groq.dto.TranscriptionResult;
import com.notecastai.voicenote.api.Mapper.VoiceNoteMapper;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import com.notecastai.voicenote.api.dto.VoiceNoteDTO;
import com.notecastai.voicenote.domain.VoiceNoteEntity;
import com.notecastai.voicenote.domain.VoiceNoteStatus;
import com.notecastai.voicenote.repo.VoiceNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceNoteHelper {

    private final VoiceNoteRepository voiceNoteRepository;
    private final VoiceNoteMapper mapper;
    private final TimestampJsonMapper timestampJsonMapper;
    private final TranscriptionService transcriptionService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(Long voiceNoteId, VoiceNoteStatus status) {
        VoiceNoteEntity entity = voiceNoteRepository.getOrThrow(voiceNoteId);
        entity.setStatus(status);
        voiceNoteRepository.saveAndFlush(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public VoiceNoteEntity saveAndFlush(VoiceNoteEntity entity) {
        return voiceNoteRepository.saveAndFlush(entity);
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
    public VoiceNoteDTO saveTranscriptionResult(
            Long noteCastId,
            String s3FileUrl,
            TranscriptionResult result
    ) {
        VoiceNoteEntity voiceNote = voiceNoteRepository.getOrThrow(noteCastId);

        // Set basic transcription data
        voiceNote.setTranscript(result.getTranscript());
        voiceNote.setLanguage(TranscriptionLanguage.fromCode(result.getLanguage()));
        voiceNote.setLanguage(TranscriptionLanguage.fromCode(result.getLanguage()));
        voiceNote.setDurationSeconds(result.getDurationSeconds());
        voiceNote.setS3FileUrl(s3FileUrl);

        // Serialize timestamps to JSON
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

        // Set metadata
        if (result.getMetadata() != null) {
            voiceNote.setTranscriptProcessingTimeMs(result.getMetadata().getProcessingTimeMs());
        }

        voiceNote.setStatus(VoiceNoteStatus.PROCESSED);
        return mapper.toDto(voiceNoteRepository.saveAndFlush(voiceNote));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateWithError(Long voiceNoteId, String errorMessage) {
        VoiceNoteEntity entity = voiceNoteRepository.getOrThrow(voiceNoteId);
        entity.setStatus(VoiceNoteStatus.FAILED);
        entity.setErrorMessage(errorMessage);
        voiceNoteRepository.saveAndFlush(entity);
    }

}