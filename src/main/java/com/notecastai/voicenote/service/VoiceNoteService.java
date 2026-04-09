package com.notecastai.voicenote.service;

import com.notecastai.voicenote.api.dto.*;
import com.notecastai.voicenote.service.command.CreateVoiceNoteCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VoiceNoteService {

    CreateVoiceNoteResponse create(CreateVoiceNoteCommand command);

    VoiceNoteDTO getById(Long id);

    Page<VoiceNoteDTO> findAll(VoiceNoteQueryParam params, Pageable pageable);

    Page<VoiceNoteShortDTO> findAllShort(VoiceNoteQueryParam params, Pageable pageable);

    void delete(Long id);

    VoiceNoteDTO addTag(Long voiceNoteId, Long tagId);

    VoiceNoteDTO removeTag(Long voiceNoteId, Long tagId);

}
