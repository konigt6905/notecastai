package com.notecastai.gamenote.service;

import com.notecastai.gamenote.api.dto.*;
import com.notecastai.gamenote.domain.GameNoteStatus;
import com.notecastai.integration.ai.dto.GameNoteAiRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GameNoteService {

    CreateGameNoteResponse create(GameNoteCreateRequest request);

    GameNoteDTO getById(Long id);

    Page<GameNoteShortDTO> findAll(GameNoteQueryParam params, Pageable pageable);

    void delete(Long id);

    void updateStatus(Long gameNoteId, GameNoteStatus status);

    void updateWithError(Long gameNoteId, String errorMessage);

    void updateWithQuestions(Long gameNoteId, List<GameQuestionDTO> questions);

    GameNoteAiRequest buildAiRequest(Long gameNoteId);

    GameNoteDTO addTag(Long gameNoteId, Long tagId);

    GameNoteDTO removeTag(Long gameNoteId, Long tagId);

}
