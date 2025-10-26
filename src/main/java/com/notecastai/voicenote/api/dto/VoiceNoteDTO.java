package com.notecastai.voicenote.api.dto;


import com.notecastai.voicenote.domain.AudioStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class VoiceNoteDTO {

    @NotNull
    private Long id;
    @NotNull
    private Long userId;
    @NotNull
    private String filename;
    @NotNull
    private String originalFilename;
    @NotNull
    private String contentType;
    @NotNull
    private Long fileSize;
    private String userInstructions;
    private String s3Path;
    @NotNull
    private AudioStatus status;
    private Long noteId;
    private String transcript;
    private String language;
    private Integer durationSeconds;
    private String errorMessage;
    @NotNull
    private Instant createdDate;
    @NotNull
    private Instant updatedDate;
}