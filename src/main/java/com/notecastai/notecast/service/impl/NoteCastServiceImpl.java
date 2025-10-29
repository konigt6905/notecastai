package com.notecastai.notecast.service.impl;

import com.notecastai.common.exeption.BusinessException;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.notecast.api.dto.*;
import com.notecastai.notecast.api.mapper.NoteCastMapper;
import com.notecastai.notecast.domain.NoteCastEntity;
import com.notecastai.notecast.domain.NoteCastStatus;
import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.event.dto.NoteCastCreatedEvent;
import com.notecastai.notecast.infrastructure.repo.NoteCastRepository;
import com.notecastai.notecast.service.NoteCastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.INVALID_REQUEST;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteCastServiceImpl implements NoteCastService {

    private final NoteCastRepository noteCastRepository;
    private final NoteRepository noteRepository;
    private final NoteCastMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public NoteCastResponseDTO create(NoteCastCreateRequest request) {
        NoteEntity note = noteRepository.getOrThrow(request.getNoteId());

        // Validate note has content
        if (note.getFormattedNote() == null || note.getFormattedNote().isBlank()) {
            throw BusinessException.of(INVALID_REQUEST.append(" Note has no formatted content"));
        }

        NoteCastEntity entity = NoteCastEntity.builder()
                .note(note)
                .status(NoteCastStatus.WAITING_FOR_TRANSCRIPT)
                .style(request.getStyle())
                .build();

        entity = noteCastRepository.save(entity);

        // Publish event for async processing
        eventPublisher.publishEvent(new NoteCastCreatedEvent(
                this,
                entity.getId(),
                note.getFormattedNote(),
                request.getStyle(),
                request.getSize()
        ));

        log.info("NoteCast created and event published: {}", entity.getId());

        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteCastResponseDTO getById(Long id) {
        NoteCastEntity entity = noteCastRepository.getOrThrow(id);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteCastResponseDTO> findAll(NoteCastQueryParam params, Pageable pageable) {
        return noteCastRepository.findAll(params, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteCastShortDTO> findAllShort(NoteCastQueryParam params, Pageable pageable) {
        Page<NoteCastEntity> entities = noteCastRepository.findAll(params, pageable);
        return entities.map(this::toShortDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteCastStyleDTO> listStyles() {
        return Arrays.stream(NoteCastStyle.values())
                .map(style -> NoteCastStyleDTO.builder()
                        .code(style.name())
                        .label(style.getLabel())
                        .promptText(style.getPromptText())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        NoteCastEntity entity = noteCastRepository.getOrThrow(id);
        noteCastRepository.delete(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(Long noteCastId, NoteCastStatus status) {
        NoteCastEntity entity = noteCastRepository.getOrThrow(noteCastId);
        entity.setStatus(status);
        noteCastRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateWithTranscript(Long noteCastId, String transcript) {
        NoteCastEntity entity = noteCastRepository.getOrThrow(noteCastId);
        entity.setTranscript(transcript);
        entity.setStatus(NoteCastStatus.WAITING_FOR_TTS);
        noteCastRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateWithError(Long noteCastId, String errorMessage) {
        NoteCastEntity entity = noteCastRepository.getOrThrow(noteCastId);
        entity.setStatus(NoteCastStatus.FAILED);
        noteCastRepository.save(entity);
        log.error("NoteCast {} failed: {}", noteCastId, errorMessage);
    }

    private NoteCastShortDTO toShortDTO(NoteCastEntity entity) {
        return NoteCastShortDTO.builder()
                .id(entity.getId())
                .noteId(entity.getNote().getId())
                .noteTitle(entity.getNote().getTitle())
                .status(entity.getStatus())
                .style(entity.getStyle())
                .createdDate(entity.getCreatedDate())
                .build();
    }
}