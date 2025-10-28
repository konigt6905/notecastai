package com.notecastai.voicenote.api.dto;

import com.notecastai.note.domain.FormateType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class VoiceNoteCreateRequest {

    @NotNull
    private MultipartFile file;
    private List<Long> tagIds; // Optional
    private String title; // Optional
    private String userInstructions; // Optional
    private FormateType formateType; // Optional

}
