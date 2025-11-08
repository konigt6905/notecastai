package com.notecastai.notecast.service;

import com.notecastai.notecast.api.dto.*;
import com.notecastai.notecast.domain.NoteCastStatus;
import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.domain.TranscriptSize;
import com.notecastai.notecast.domain.TtsVoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoteCastService {

    NoteCastResponseDTO create(NoteCastCreateRequest request);

    NoteCastResponseDTO getById(Long id);

    Page<NoteCastResponseDTO> findAll(NoteCastQueryParam params, Pageable pageable);

    Page<NoteCastShortDTO> findAllShort(NoteCastQueryParam params, Pageable pageable);

    List<NoteCastStyleDTO> listStyles();

    void delete(Long id);

    void updateStatus(Long noteCastId, NoteCastStatus status);

    void updateWithTranscript(Long noteCastId, String transcript);

    void updateWithError(Long noteCastId, String errorMessage);

    NoteCastResponseDTO regenerate(Long id, NoteCastStyle style, TtsVoice voice, TranscriptSize size);

    NoteCastShareResponse generateShareLink(Long id);

    NoteCastResponseDTO addTag(Long noteCastId, Long tagId);

    NoteCastResponseDTO removeTag(Long noteCastId, Long tagId);

}