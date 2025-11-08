package com.notecastai.voicenote.service.impl;

import com.notecastai.common.util.FileValidationUtil;
import com.notecastai.common.util.SecurityUtils;
import com.notecastai.note.api.dto.CreateNoteRequest;
import com.notecastai.note.api.dto.NoteDTO;
import com.notecastai.note.domain.NoteType;
import com.notecastai.note.service.NoteService;
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

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceNoteServiceImpl implements VoiceNoteService {

    private final VoiceNoteRepository voiceNoteRepository;
    private final UserRepository userRepository;
    private final VoiceNoteMapper mapper;
    private final VoiceNoteProcessorOrchestrator voiceNoteProcessorOrchestrator;
    private final NoteService noteService;
    private final VoiceNoteHelper voiceNoteHelper;

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

        entity = voiceNoteHelper.saveAndFlush(entity);
        Long voiceNoteId = entity.getId();

        VoiceNoteDTO voiceNote = voiceNoteProcessorOrchestrator.processVoiceNote(voiceNoteId, file, user.getPreferredLanguage());

        NoteDTO note = noteService.create(CreateNoteRequest.builder()
                .title(request.getTitle())
                .tagIds(request.getTagIds())
                .type(NoteType.VOICENOTE)
                .knowledgeBase(voiceNote.getTranscript())
                .formateType(request.getFormateType())
                .instructions(request.getUserInstructions())
                .build());

        return UploadVoiceNoteResponse.builder()
                .voiceNote(voiceNote)
                .note(note)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VoiceNoteDTO getById(Long id) {
        VoiceNoteEntity entity = voiceNoteRepository.getOrThrow(id);
        return mapper.toDto(entity);
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
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .build();
    }
}