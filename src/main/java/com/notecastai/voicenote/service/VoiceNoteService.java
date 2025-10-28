package com.notecastai.voicenote.service;

import com.notecastai.integration.ai.provider.groq.dto.TranscriptionResult;
import com.notecastai.voicenote.api.dto.*;
import com.notecastai.voicenote.domain.VoiceNoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public interface VoiceNoteService {

    UploadVoiceNoteResponse upload(VoiceNoteCreateRequest request);

    VoiceNoteDTO getById(Long id);

    Page<VoiceNoteDTO> findAll(VoiceNoteQueryParam params, Pageable pageable);

    Page<VoiceNoteShortDTO> findAllShort(VoiceNoteQueryParam params, Pageable pageable);

    void deactivate(Long id);

    void updateStatus(Long voiceNoteId, VoiceNoteStatus status);

    void saveTranscriptionResult(Long voiceNoteId, String s3FileUrl, TranscriptionResult transcription);

    void updateWithError(Long voiceNoteId, String errorMessage);

    CompletableFuture<TranscriptionResult> transcribeAsync(
            InputStream audioStream,
            String filename,
            String contentType,
            TranscriptionLanguage preferredLanguage);

}
