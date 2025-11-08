package com.notecastai.voicenote.service.impl;

import com.notecastai.common.util.FileValidationUtil;
import com.notecastai.common.util.SecurityUtils;
import com.notecastai.integration.storage.StorageService;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.repo.TagRepository;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import com.notecastai.voicenote.api.Mapper.VoiceNoteMapper;
import com.notecastai.voicenote.api.dto.*;
import com.notecastai.voicenote.domain.VoiceNoteEntity;
import com.notecastai.voicenote.domain.VoiceNoteStatus;
import com.notecastai.voicenote.repo.VoiceNoteRepository;
import com.notecastai.voicenote.service.VoiceNoteProcessorOrchestrator;
import com.notecastai.voicenote.service.VoiceNoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceNoteServiceImpl implements VoiceNoteService {

    private final VoiceNoteRepository voiceNoteRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final VoiceNoteMapper mapper;
    private final VoiceNoteProcessorOrchestrator voiceNoteProcessorOrchestrator;
    private final VoiceNoteHelper voiceNoteHelper;
    private final StorageService s3StorageService;

    @Override
    @Transactional
    public UploadVoiceNoteResponse upload(VoiceNoteCreateRequest request) {
        MultipartFile file = request.getFile();

        // Validate file
        FileValidationUtil.validateAudioFile(file);

        UserEntity user = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow());

        // Create entity with PENDING status
        String filename = generateUniqueFilename(file.getOriginalFilename());
        VoiceNoteEntity entity = VoiceNoteEntity.builder()
                .user(user)
                .filename(filename)
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .language(user.getPreferredLanguage())
                .userInstructions(request.getUserInstructions())
                .status(VoiceNoteStatus.PENDING)
                .build();

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<TagEntity> tags = resolveAndValidateTags(user.getId(), request.getTagIds());
            entity.setTags(tags);
        }

        entity = voiceNoteHelper.saveAndFlush(entity);

        VoiceNoteDTO voiceNote = voiceNoteProcessorOrchestrator.processVoiceNote(entity.getId(), file, user.getPreferredLanguage(), request);

        //reattach
        entity = voiceNoteRepository.getOrThrow(entity.getId());
        entity.setNote(noteRepository.getOrThrow(voiceNote.getNote().getId()));

        return UploadVoiceNoteResponse.builder()
                .voiceNote(mapper.toDto(voiceNoteRepository.save(entity)))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VoiceNoteDTO getById(Long id) {
        VoiceNoteEntity entity = voiceNoteRepository.getOrThrow(id);
        VoiceNoteDTO dto = mapper.toDto(entity);
        dto.setS3Path(s3StorageService.presignedGet(entity.getS3FileUrl()));
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
        voiceNoteRepository.delete(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(Long voiceNoteId, VoiceNoteStatus status) {
        VoiceNoteEntity entity = voiceNoteRepository.getOrThrow(voiceNoteId);
        entity.setStatus(status);
        voiceNoteRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateWithError(Long voiceNoteId, String errorMessage) {
        VoiceNoteEntity entity = voiceNoteRepository.getOrThrow(voiceNoteId);
        entity.setStatus(VoiceNoteStatus.FAILED);
        entity.setErrorMessage(errorMessage);
        voiceNoteRepository.save(entity);
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
                        .map(tag -> com.notecastai.tag.api.dto.TagDTO.builder()
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
        TagEntity tag = tagRepository.getById(tagId);

        voiceNote.getTags().add(tag);

        VoiceNoteEntity savedVoiceNote = voiceNoteRepository.save(voiceNote);
        log.info("Tag {} added to voice note {}", tagId, voiceNoteId);

        VoiceNoteDTO dto = mapper.toDto(savedVoiceNote);
        dto.setS3Path(s3StorageService.presignedGet(savedVoiceNote.getS3FileUrl()));
        return dto;
    }

    @Override
    @Transactional
    public VoiceNoteDTO removeTag(Long voiceNoteId, Long tagId) {
        VoiceNoteEntity voiceNote = voiceNoteRepository.getOrThrow(voiceNoteId);

        voiceNote.getTags().removeIf(tag -> tag.getId().equals(tagId));

        VoiceNoteEntity savedVoiceNote = voiceNoteRepository.save(voiceNote);
        log.info("Tag {} removed from voice note {}", tagId, voiceNoteId);

        VoiceNoteDTO dto = mapper.toDto(savedVoiceNote);
        dto.setS3Path(s3StorageService.presignedGet(savedVoiceNote.getS3FileUrl()));
        return dto;
    }

    private Set<TagEntity> resolveAndValidateTags(Long userId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return Set.of();
        Set<TagEntity> result = new HashSet<>();
        for (Long tagId : tagIds) {
            if (tagId == null) continue;
            TagEntity tag = tagRepository.findByIdAndUserOrThrow(tagId, userId);
            result.add(tag);
        }
        return result;
    }
}