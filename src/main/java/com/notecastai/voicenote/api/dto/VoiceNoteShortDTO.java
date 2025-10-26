package com.notecastai.voicenote.api.dto;

import com.notecastai.voicenote.domain.AudioStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class VoiceNoteShortDTO {

    @NotNull
    private Long id;
    @NotNull
    private String originalFilename;
    @NotNull
    private Long fileSize;
    @NotNull
    private AudioStatus status;
    private Long noteId;
    @NotNull
    private Instant createdDate;
    @NotNull
    private Instant updatedDate;
}