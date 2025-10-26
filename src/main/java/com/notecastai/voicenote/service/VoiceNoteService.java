package com.notecastai.voicenote.service;

import com.notecastai.integration.ai.TranscriptionService;
import com.notecastai.voicenote.api.dto.VoiceNoteCreateRequest;
import com.notecastai.voicenote.api.dto.VoiceNoteDTO;
import com.notecastai.voicenote.api.dto.VoiceNoteQueryParam;
import com.notecastai.voicenote.api.dto.VoiceNoteShortDTO;
import com.notecastai.voicenote.domain.AudioStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VoiceNoteService {

    VoiceNoteDTO upload(VoiceNoteCreateRequest request);

    VoiceNoteDTO getById(Long id);

    Page<VoiceNoteDTO> findAll(VoiceNoteQueryParam params, Pageable pageable);

    Page<VoiceNoteShortDTO> findAllShort(VoiceNoteQueryParam params, Pageable pageable);

    void delete(Long id);

    void updateStatus(Long voiceNoteId, AudioStatus status);

    void updateWithResults(Long voiceNoteId, String s3Path, TranscriptionService.TranscriptionResult transcription);

    void updateWithError(Long voiceNoteId, String errorMessage);

}
