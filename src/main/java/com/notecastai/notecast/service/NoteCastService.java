package com.notecastai.notecast.service;

import com.notecastai.notecast.api.dto.*;
import com.notecastai.notecast.domain.NoteCastStatus;
import com.notecastai.notecast.service.command.CreateNoteCastCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoteCastService {

    NoteCastResponseDTO create(CreateNoteCastCommand command);

    NoteCastResponseDTO getById(Long id);

    Page<NoteCastResponseDTO> findAll(NoteCastQueryParam params, Pageable pageable);

    Page<NoteCastShortDTO> findAllShort(NoteCastQueryParam params, Pageable pageable);

    List<NoteCastStyleDTO> listStyles();

    void delete(Long id);

    void updateStatus(Long noteCastId, NoteCastStatus status);

    void updateWithTranscript(Long noteCastId, String transcript);

    void updateWithAudio(Long noteCastId, String s3FileKey, Integer durationSeconds, Long processingTimeMs);

    void updateWithError(Long noteCastId, String errorMessage);

    NoteCastShareResponse generateShareLink(Long id);

    NoteCastResponseDTO addTag(Long noteCastId, Long tagId);

    NoteCastResponseDTO removeTag(Long noteCastId, Long tagId);

}
