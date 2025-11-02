package com.notecastai.voicenote.api.dto;


import com.notecastai.voicenote.domain.VoiceNoteStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private VoiceNoteStatus status;
    private Long noteId;
    private String transcript;
    private String language;
    private Integer durationSeconds;
    @NotNull
    private Instant createdDate;
    @NotNull
    private Instant updatedDate;

}