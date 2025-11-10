package com.notecastai.notecast.service.impl;

import com.notecastai.common.exeption.BusinessException;
import com.notecastai.common.util.SecurityUtils;
import com.notecastai.config.TtsVoiceProperties;
import com.notecastai.integration.storage.StorageService;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.notecast.api.dto.*;
import com.notecastai.notecast.api.mapper.NoteCastMapper;
import com.notecastai.notecast.domain.*;
import com.notecastai.notecast.event.dto.NoteCastCreatedEvent;
import com.notecastai.notecast.infrastructure.repo.NoteCastRepository;
import com.notecastai.notecast.service.NoteCastService;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.repo.TagRepository;
import com.notecastai.user.service.UserService;
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
    private final UserService userService;
    private final NoteCastMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TagRepository tagRepository;
    private final TtsVoiceProperties ttsVoiceProperties;
    private final StorageService s3StorageService;

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

        TtsVoice resolvedVoice = resolveVoiceForProvider(request.getVoice(), ttsVoiceProperties.getVoiceProvider());

        // Determine title: use request title if provided, otherwise fall back to note title
        String title = request.getTitle();
        if (title == null || title.isBlank()) {
            log.info("Title is null or blank, falling back to note title: {}", note.getTitle());
            title = note.getTitle();
        }

        NoteCastEntity entity = NoteCastEntity.builder()
                .note(note)
                .title(title)
                .status(NoteCastStatus.WAITING_FOR_TRANSCRIPT)
                .style(request.getStyle())
                .size(request.getSize())
                .voice(resolvedVoice)
                .build();

        entity = noteCastRepository.save(entity);

        // Publish event for async processing
        eventPublisher.publishEvent(new NoteCastCreatedEvent(
                this,
                entity.getId(),
                note.getFormattedNote(),
                request.getStyle(),
                request.getSize(),
                request.getCustomInstructions(),
                entity.getVoice()
        ));

        log.info("NoteCast created and event published: {} with voice: {}, size: {}",
                entity.getId(), resolvedVoice, request.getSize());

        return mapper.toDto(entity);
    }

    private TtsVoice getUserDefaultVoice(TtsVoiceProvider provider) {
        TtsVoice userVoice = userService.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow()).getDefaultVoice();
        if (userVoice != null && userVoice.supportsProvider(provider)) {
            return userVoice;
        }
        return TtsVoice.getDefault(provider);
    }

    private TtsVoice resolveVoiceForProvider(TtsVoice requested, TtsVoiceProvider provider) {
        if (requested != null && requested.supportsProvider(provider)) {
            return requested;
        }
        return getUserDefaultVoice(provider);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteCastResponseDTO getById(Long id) {
        NoteCastEntity entity = noteCastRepository.getOrThrow(id);
        return mapper.toDto(entity).withS3FileUrl(s3StorageService.presignedAndGet(entity.getS3FileUrl()));
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
    public void updateWithAudio(Long noteCastId,
                                String s3FileKey,
                                Integer durationSeconds,
                                Long processingTimeMs) {
        NoteCastEntity entity = noteCastRepository.getOrThrow(noteCastId);
        entity.setS3FileUrl(s3FileKey);
        entity.setDurationSeconds(durationSeconds);
        entity.setProcessingTime(processingTimeMs != null
                ? Math.max(1L, Math.round(processingTimeMs / 1000.0))
                : null);
        entity.setStatus(NoteCastStatus.PROCESSED);
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
        NoteCastResponseDTO fullDto = mapper.toDto(entity);
        return NoteCastShortDTO.builder()
                .id(entity.getId())
                .noteId(entity.getNote().getId())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .style(entity.getStyle())
                .tags(fullDto.getTags())
                .createdDate(entity.getCreatedDate())
                .build();
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

    @Override
    @Transactional
    public NoteCastResponseDTO addTag(Long noteCastId, Long tagId) {
        NoteCastEntity noteCast = noteCastRepository.getOrThrow(noteCastId);
        TagEntity tag = tagRepository.getById(tagId);

        noteCast.getTags().add(tag);
        NoteCastEntity savedNoteCast = noteCastRepository.save(noteCast);

        log.info("Tag {} added to notecast {}", tagId, noteCastId);
        return mapper.toDto(savedNoteCast);
    }

    @Override
    @Transactional
    public NoteCastResponseDTO removeTag(Long noteCastId, Long tagId) {
        NoteCastEntity noteCast = noteCastRepository.getOrThrow(noteCastId);
        TagEntity tag = tagRepository.getById(tagId);

        noteCast.getTags().remove(tag);
        NoteCastEntity savedNoteCast = noteCastRepository.save(noteCast);

        log.info("Tag {} removed from notecast {}", tagId, noteCastId);
        return mapper.toDto(savedNoteCast);
    }
}
