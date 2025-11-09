package com.notecastai.voicenote.service;

import com.notecastai.voicenote.api.dto.*;
import com.notecastai.voicenote.domain.VoiceNoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VoiceNoteService {

    CreateVoiceNoteResponse create(VoiceNoteCreateRequest request);

    VoiceNoteDTO getById(Long id);

    Page<VoiceNoteDTO> findAll(VoiceNoteQueryParam params, Pageable pageable);

    Page<VoiceNoteShortDTO> findAllShort(VoiceNoteQueryParam params, Pageable pageable);

    void delete(Long id);

    void updateStatus(Long voiceNoteId, VoiceNoteStatus status);

    void updateWithError(Long voiceNoteId, String errorMessage);

    VoiceNoteDTO addTag(Long voiceNoteId, Long tagId);

    VoiceNoteDTO removeTag(Long voiceNoteId, Long tagId);

}
