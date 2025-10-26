package com.notecastai.voicenote.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class VoiceNoteCreateRequest {

    @NotNull
    private MultipartFile file;

    private String userInstructions;
}
