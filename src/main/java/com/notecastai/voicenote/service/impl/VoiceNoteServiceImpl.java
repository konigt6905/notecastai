package com.notecastai.voicenote.service.impl;

import com.notecastai.common.util.OwnershipVerifier;
import com.notecastai.integration.storage.StorageService;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.infrastructure.repo.TagRepository;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.service.UserService;
import com.notecastai.voicenote.api.mapper.VoiceNoteMapper;
import com.notecastai.voicenote.api.dto.*;
import com.notecastai.voicenote.domain.VoiceNoteEntity;
import com.notecastai.voicenote.domain.VoiceNoteStatus;
import com.notecastai.voicenote.domain.event.VoiceNoteCreatedEvent;
import com.notecastai.voicenote.infrastructure.repo.VoiceNoteRepository;
import com.notecastai.voicenote.service.VoiceNoteService;
import com.notecastai.voicenote.service.command.CreateVoiceNoteCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceNoteServiceImpl implements VoiceNoteService {

    private final VoiceNoteRepository voiceNoteRepository;
    private final UserService userService;
    private final TagRepository tagRepository;
    private final VoiceNoteMapper mapper;
    private final VoiceNoteHelper voiceNoteHelper;
    private final StorageService s3StorageService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CreateVoiceNoteResponse create(CreateVoiceNoteCommand command) {
        UserEntity user = userService.getCurrentUserReference();

        String filename = generateUniqueFilename(command.getOriginalFilename());
        VoiceNoteEntity entity = VoiceNoteEntity.builder()
                .user(user)
                .filename(filename)
                .originalFilename(command.getOriginalFilename())
                .contentType(command.getContentType())
                .fileSize(command.getFileSize())
                .language(user.getPreferredLanguage())
                .userInstructions(command.getUserInstructions())
                .status(VoiceNoteStatus.PENDING)
                .build();

        if (command.getTagIds() != null && !command.getTagIds().isEmpty()) {
            Set<TagEntity> tags = tagRepository.resolveAndValidateForUser(command.getTagIds(), user.getId());
            entity.setTags(tags);
        }

        VoiceNoteEntity saved = voiceNoteRepository.save(entity);

        log.info("VoiceNote created: id={}, status={}", saved.getId(), saved.getStatus());

        eventPublisher.publishEvent(VoiceNoteCreatedEvent.builder()
                .voiceNoteId(saved.getId())
                .audioBytes(command.getAudioBytes())
                .originalFilename(command.getOriginalFilename())
                .contentType(command.getContentType())
                .fileSize(command.getFileSize())
                .preferredLanguage(user.getPreferredLanguage())
                .title(command.getTitle())
                .userInstructions(command.getUserInstructions())
                .tagIds(command.getTagIds())
                .formatType(command.getFormatType())
                .build());

        return CreateVoiceNoteResponse.builder()
                .id(saved.getId())
                .status(saved.getStatus())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VoiceNoteDTO getById(Long id) {
        VoiceNoteEntity entity = voiceNoteRepository.getOrThrow(id);
        OwnershipVerifier.verify(entity.getUser().getId());
        VoiceNoteDTO dto = mapper.toDto(entity);
        dto.setS3Path(s3StorageService.presignedAndGet(entity.getS3FileUrl()));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoiceNoteDTO> findAll(VoiceNoteQueryParam params, Pageable pageable) {
        return voiceNoteRepository.findAll(params, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoiceNoteShortDTO> findAllShort(VoiceNoteQueryParam params, Pageable pageable) {
        Page<VoiceNoteEntity> entities = voiceNoteRepository.findAll(params, pageable);
        return entities.map(this::toShortDTO);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        VoiceNoteEntity entity = voiceNoteRepository.getOrThrow(id);
        OwnershipVerifier.verify(entity.getUser().getId());
        voiceNoteRepository.delete(entity);
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }

    private VoiceNoteShortDTO toShortDTO(VoiceNoteEntity entity) {
        return VoiceNoteShortDTO.builder()
                .id(entity.getId())
                .originalFilename(entity.getOriginalFilename())
                .fileSize(entity.getFileSize())
                .status(entity.getStatus())
                .noteId(entity.getNote() != null ? entity.getNote().getId() : null)
                .tags(entity.getTags().stream()
                        .map(tag -> TagDTO.builder()
                                .id(tag.getId())
                                .userId(tag.getUser().getId())
                                .name(tag.getName())
                                .build())
                        .collect(Collectors.toList()))
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .build();
    }

    @Override
    @Transactional
    public VoiceNoteDTO addTag(Long voiceNoteId, Long tagId) {
        VoiceNoteEntity voiceNote = voiceNoteRepository.getOrThrow(voiceNoteId);
        OwnershipVerifier.verify(voiceNote.getUser().getId());

        Long currentUserId = userService.getCurrentUserId();
        TagEntity tag = tagRepository.findByIdAndUserOrThrow(tagId, currentUserId);

        voiceNote.addTag(tag);

        VoiceNoteEntity savedVoiceNote = voiceNoteRepository.save(voiceNote);
        log.info("Tag {} added to voice note {}", tagId, voiceNoteId);

        VoiceNoteDTO dto = mapper.toDto(savedVoiceNote);
        dto.setS3Path(s3StorageService.presignedAndGet(savedVoiceNote.getS3FileUrl()));
        return dto;
    }

    @Override
    @Transactional
    public VoiceNoteDTO removeTag(Long voiceNoteId, Long tagId) {
        VoiceNoteEntity voiceNote = voiceNoteRepository.getOrThrow(voiceNoteId);
        OwnershipVerifier.verify(voiceNote.getUser().getId());

        voiceNote.removeTagById(tagId);

        VoiceNoteEntity savedVoiceNote = voiceNoteRepository.save(voiceNote);
        log.info("Tag {} removed from voice note {}", tagId, voiceNoteId);

        VoiceNoteDTO dto = mapper.toDto(savedVoiceNote);
        dto.setS3Path(s3StorageService.presignedAndGet(savedVoiceNote.getS3FileUrl()));
        return dto;
    }

}