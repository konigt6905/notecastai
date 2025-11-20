package com.notecastai.gamenote.service;

import com.notecastai.gamenote.api.dto.*;
import com.notecastai.gamenote.domain.GameNoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GameNoteService {

    CreateGameNoteResponse create(GameNoteCreateRequest request);

    GameNoteDTO getById(Long id);

    Page<GameNoteShortDTO> findAll(GameNoteQueryParam params, Pageable pageable);

    void delete(Long id);

    void updateStatus(Long gameNoteId, GameNoteStatus status);

    void updateWithError(Long gameNoteId, String errorMessage);

    GameNoteDTO addTag(Long gameNoteId, Long tagId);

    GameNoteDTO removeTag(Long gameNoteId, Long tagId);

}
