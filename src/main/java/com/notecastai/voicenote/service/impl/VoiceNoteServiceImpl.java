package com.notecastai.voicenote.service.impl;

import com.notecastai.common.util.FileValidationUtil;
import com.notecastai.common.util.SecurityUtils;
import com.notecastai.integration.ai.TranscriptionService;
import com.notecastai.integration.storage.StorageService;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import com.notecastai.voicenote.api.Mapper.VoiceNoteMapper;
import com.notecastai.voicenote.api.dto.VoiceNoteCreateRequest;
import com.notecastai.voicenote.api.dto.VoiceNoteDTO;
import com.notecastai.voicenote.api.dto.VoiceNoteQueryParam;
import com.notecastai.voicenote.api.dto.VoiceNoteShortDTO;
import com.notecastai.voicenote.domain.AudioStatus;
import com.notecastai.voicenote.domain.VoiceNoteEntity;
import com.notecastai.voicenote.infrastructure.repo.VoiceNoteRepository;
import com.notecastai.voicenote.service.VoiceNoteProcessor;
import com.notecastai.voicenote.service.VoiceNoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceNoteServiceImpl implements VoiceNoteService {

    private final VoiceNoteRepository voiceNoteRepository;
    private final UserRepository userRepository;
    private final VoiceNoteMapper mapper;
    private final StorageService storageService;
    private final VoiceNoteProcessor voiceNoteProcessor;

    @Override
    @Transactional
    public VoiceNoteDTO upload(VoiceNoteCreateRequest request) {
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
                .userInstructions(request.getUserInstructions())
                .status(AudioStatus.PENDING)
                .build();

        entity = voiceNoteRepository.save(entity);
        Long voiceNoteId = entity.getId();

        processAsync(voiceNoteId, file);

        return mapper.toDto(entity);
    }

    private void processAsync(Long voiceNoteId, MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes();
            String s3Key = buildS3Key(voiceNoteId, file.getOriginalFilename());

            updateStatus(voiceNoteId, AudioStatus.PROCESSING);

            // Start both async operations in parallel
            CompletableFuture<String> uploadFuture = storageService.putAsync(
                    s3Key,
                    new ByteArrayInputStream(fileBytes),
                    file.getSize(),
                    file.getContentType()
            );

            CompletableFuture<TranscriptionService.TranscriptionResult> transcriptionFuture =
                    voiceNoteProcessor.transcribeAsync(
                            new ByteArrayInputStream(fileBytes),
                            file.getOriginalFilename(),
                            file.getContentType()
                    );

            // Join both futures and update entity
            CompletableFuture.allOf(uploadFuture, transcriptionFuture)
                    .thenAccept(v -> {
                        try {
                            String s3Path = uploadFuture.join();
                            TranscriptionService.TranscriptionResult transcription = transcriptionFuture.join();

                            updateWithResults(voiceNoteId, s3Path, transcription);
                            log.info("Voice note processing completed successfully: {}", voiceNoteId);
                        } catch (Exception e) {
                            log.error("Error completing voice note processing: {}", voiceNoteId, e);
                            updateWithError(voiceNoteId, "Error completing processing: " + e.getMessage());
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("Error processing voice note: {}", voiceNoteId, ex);
                        updateWithError(voiceNoteId, "Processing failed: " + ex.getMessage());
                        return null;
                    });

        } catch (IOException e) {
            log.error("Failed to read file bytes for voice note: {}", voiceNoteId, e);
            updateWithError(voiceNoteId, "Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing voice note: {}", voiceNoteId, e);
            updateWithError(voiceNoteId, "Unexpected error: " + e.getMessage());
        }
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

        // Delete from S3 if exists
        if (entity.getS3Path() != null) {
            try {
                storageService.delete(entity.getS3Path());
            } catch (Exception e) {
                log.error("Failed to delete file from S3: {}", entity.getS3Path(), e);
            }
        }

        voiceNoteRepository.delete(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(Long voiceNoteId, AudioStatus status) {
        VoiceNoteEntity entity = voiceNoteRepository.getOrThrow(voiceNoteId);
        entity.setStatus(status);
        voiceNoteRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateWithResults(Long voiceNoteId, String s3Path, TranscriptionService.TranscriptionResult transcription) {
        VoiceNoteEntity entity = voiceNoteRepository.getOrThrow(voiceNoteId);
        entity.setS3Path(s3Path);
        entity.setTranscript(transcription.transcript());
        entity.setLanguage(transcription.language());
        entity.setDurationSeconds(transcription.durationSeconds());
        entity.setStatus(AudioStatus.PROCESSED);
        voiceNoteRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateWithError(Long voiceNoteId, String errorMessage) {
        VoiceNoteEntity entity = voiceNoteRepository.getOrThrow(voiceNoteId);
        entity.setStatus(AudioStatus.FAILED);
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

    private String buildS3Key(Long voiceNoteId, String filename) {
        return String.format("voice-notes/%d/%s", voiceNoteId, filename);
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