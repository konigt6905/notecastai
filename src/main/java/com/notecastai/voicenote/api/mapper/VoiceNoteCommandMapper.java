package com.notecastai.voicenote.api.mapper;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.common.util.FileValidationUtil;
import com.notecastai.voicenote.api.dto.VoiceNoteCreateRequest;
import com.notecastai.voicenote.service.command.CreateVoiceNoteCommand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VoiceNoteCommandMapper {

    default CreateVoiceNoteCommand toCommand(VoiceNoteCreateRequest request) {
        MultipartFile file = FileValidationUtil.validateAndNormalizeIfNeeded(request.getFile());

        byte[] audioBytes;
        try {
            audioBytes = file.getBytes();
        } catch (IOException e) {
            throw BusinessException.of(BusinessException.BusinessCode.INVALID_REQUEST
                    .append(" Failed to read uploaded file"));
        }

        return CreateVoiceNoteCommand.builder()
                .audioBytes(audioBytes)
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .tagIds(request.getTagIds())
                .title(request.getTitle())
                .userInstructions(request.getUserInstructions())
                .language(request.getLanguage())
                .formatType(request.getFormatType())
                .build();
    }
}
