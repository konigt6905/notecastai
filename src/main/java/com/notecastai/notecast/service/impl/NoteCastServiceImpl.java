package com.notecastai.notecast.service.impl;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.common.util.OwnershipVerifier;
import com.notecastai.config.TtsVoiceProperties;
import com.notecastai.integration.storage.StorageService;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.notecast.api.dto.*;
import com.notecastai.notecast.api.mapper.NoteCastMapper;
import com.notecastai.notecast.service.command.CreateNoteCastCommand;
import com.notecastai.notecast.domain.*;
import com.notecastai.notecast.domain.event.NoteCastCreatedEvent;
import com.notecastai.notecast.infrastructure.repo.NoteCastRepository;
import com.notecastai.notecast.service.NoteCastService;
import com.notecastai.notecast.service.factory.NoteCastEntityFactory;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.infrastructure.repo.TagRepository;
import com.notecastai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.notecastai.common.exception.BusinessException.BusinessCode.INVALID_REQUEST;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteCastServiceImpl implements NoteCastService {

    private final NoteCastRepository noteCastRepository;
    private final UserService userService;
    private final NoteCastMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TagRepository tagRepository;
    private final StorageService s3StorageService;
    private final NoteCastEntityFactory noteCastEntityFactory;

    @Value("${application.domain}")
    private String applicationDomain;

    @Override
    @Transactional
    public NoteCastResponseDTO create(CreateNoteCastCommand command) {
        NoteCastEntityFactory.NoteCastWithSource result = noteCastEntityFactory.createFromCommand(command);
        NoteCastEntity entity = noteCastRepository.save(result.entity());

        eventPublisher.publishEvent(new NoteCastCreatedEvent(
                this,
                entity.getId(),
                result.sourceNote().getFormattedNote(),
                command.getStyle(),
                command.getSize(),
                command.getCustomInstructions(),
                entity.getVoice()
        ));

        log.info("NoteCast created and event published: {} with voice: {}, size: {}",
                entity.getId(), entity.getVoice(), command.getSize());

        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteCastResponseDTO getById(Long id) {
        NoteCastEntity entity = noteCastRepository.getOrThrow(id);
        OwnershipVerifier.verify(entity.getNote().getUser().getId());
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
        OwnershipVerifier.verify(entity.getNote().getUser().getId());
        noteCastRepository.delete(entity);
    }

    @Transactional
    public void updateStatus(Long noteCastId, NoteCastStatus status) {
        NoteCastEntity entity = noteCastRepository.getOrThrow(noteCastId);
        entity.setStatus(status);
        noteCastRepository.save(entity);
    }

    @Transactional
    public void updateWithTranscript(Long noteCastId, String transcript) {
        NoteCastEntity entity = noteCastRepository.getOrThrow(noteCastId);
        entity.markTranscriptReady(transcript);
        noteCastRepository.save(entity);
    }

    @Transactional
    public void updateWithAudio(Long noteCastId,
                                String s3FileKey,
                                Integer durationSeconds,
                                Long processingTimeMs) {
        NoteCastEntity entity = noteCastRepository.getOrThrow(noteCastId);
        entity.markAudioReady(s3FileKey, durationSeconds, processingTimeMs);
        noteCastRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateWithError(Long noteCastId, String errorMessage) {
        NoteCastEntity entity = noteCastRepository.getOrThrow(noteCastId);
        entity.markAsFailed();
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
        OwnershipVerifier.verify(noteCast.getNote().getUser().getId());

        // Check if notecast is in a shareable state
        if (noteCast.getStatus() != NoteCastStatus.PROCESSED) {
            throw BusinessException.of(INVALID_REQUEST
                    .append(" Notecast must be completed before sharing"));
        }

        Instant now = Instant.now();
        if (noteCast.getShareToken() == null ||
                noteCast.getShareExpiresAt() == null ||
                noteCast.getShareExpiresAt().isBefore(now)) {

            // Generate new token
            String token = UUID.randomUUID().toString().replace("-", "");

            // Set expiration to 30 days from now
            Instant expiresAt = now.plus(30, ChronoUnit.DAYS);

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
        OwnershipVerifier.verify(noteCast.getNote().getUser().getId());

        Long currentUserId = userService.getCurrentUserId();
        TagEntity tag = tagRepository.findByIdAndUserOrThrow(tagId, currentUserId);

        noteCast.addTag(tag);
        NoteCastEntity savedNoteCast = noteCastRepository.save(noteCast);

        log.info("Tag {} added to notecast {}", tagId, noteCastId);
        return mapper.toDto(savedNoteCast);
    }

    @Override
    @Transactional
    public NoteCastResponseDTO removeTag(Long noteCastId, Long tagId) {
        NoteCastEntity noteCast = noteCastRepository.getOrThrow(noteCastId);
        OwnershipVerifier.verify(noteCast.getNote().getUser().getId());

        noteCast.removeTagById(tagId);
        NoteCastEntity savedNoteCast = noteCastRepository.save(noteCast);

        log.info("Tag {} removed from notecast {}", tagId, noteCastId);
        return mapper.toDto(savedNoteCast);
    }

}
