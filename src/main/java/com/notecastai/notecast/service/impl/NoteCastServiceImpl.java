package com.notecastai.notecast.service.impl;

import com.notecastai.common.exeption.BusinessException;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.notecast.api.dto.*;
import com.notecastai.notecast.api.mapper.NoteCastMapper;
import com.notecastai.notecast.domain.*;
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
    private final com.notecastai.user.infrastructure.repo.UserRepository userRepository;
    private final NoteCastMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @org.springframework.beans.factory.annotation.Value("${application.domain}")
    private String applicationDomain;

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
                .size(request.getSize())
                .voice(request.getVoice() == null? TtsVoice.getDefault() : request.getVoice())
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

        log.info("NoteCast created and event published: {} with voice: {}, size: {}",
                entity.getId(), request.getVoice(), request.getSize());

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

    @Override
    @Transactional
    public NoteCastResponseDTO regenerate(Long id, NoteCastStyle style, TtsVoice voice, TranscriptSize size) {
        NoteCastEntity noteCast = noteCastRepository.getOrThrow(id);

        if (style != null) {
            noteCast.setStyle(style);
        }

        if (voice != null) {
            noteCast.setVoice(voice);
        }

        if (size != null) {
            noteCast.setSize(size);
        }

        noteCast.setStatus(NoteCastStatus.WAITING_FOR_TRANSCRIPT);
        noteCast.setTranscript(null); // Clear old transcript
        noteCast.setS3FileUrl(null); // Clear old audio
        noteCast.setErrorMessage(null); // Clear any previous errors

        noteCast = noteCastRepository.save(noteCast);

        // Publish event to trigger async regeneration
        NoteEntity note = noteCast.getNote();
        eventPublisher.publishEvent(new NoteCastCreatedEvent(
                this,
                noteCast.getId(),
                note.getFormattedNote(),
                noteCast.getStyle(),
                noteCast.getSize()
        ));

        log.info("Regeneration triggered for notecast {} with style: {}, voice: {}, size: {}",
                id, noteCast.getStyle(), noteCast.getVoice(), noteCast.getSize());

        return mapper.toDto(noteCast);
    }

    @Override
    @Transactional
    public NoteCastShareResponse generateShareLink(Long id) {
        NoteCastEntity noteCast = noteCastRepository.getOrThrow(id);

        // Check if notecast is in a shareable state
        if (noteCast.getStatus() != NoteCastStatus.PROCESSED) {
            throw com.notecastai.common.exeption.BusinessException.of(
                com.notecastai.common.exeption.BusinessException.BusinessCode.INVALID_REQUEST
                    .append(" Notecast must be completed before sharing")
            );
        }

        java.time.Instant now = java.time.Instant.now();
        if (noteCast.getShareToken() == null ||
            noteCast.getShareExpiresAt() == null ||
            noteCast.getShareExpiresAt().isBefore(now)) {

            // Generate new token
            String token = java.util.UUID.randomUUID().toString().replace("-", "");

            // Set expiration to 30 days from now
            java.time.Instant expiresAt = now.plus(30, java.time.temporal.ChronoUnit.DAYS);

            noteCast.setShareToken(token);
            noteCast.setShareExpiresAt(expiresAt);

            noteCast = noteCastRepository.save(noteCast);
        }

        // Build share URL using configured application domain
        String shareUrl = applicationDomain + "/public/notecast/" + noteCast.getShareToken();

        return NoteCastShareResponse.builder()
                .shareUrl(shareUrl)
                .shareToken(noteCast.getShareToken())
                .expiresAt(noteCast.getShareExpiresAt())
                .build();
    }
}