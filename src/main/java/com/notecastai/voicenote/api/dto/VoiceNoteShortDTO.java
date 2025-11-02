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
public class VoiceNoteShortDTO {

    @NotNull
    private Long id;
    @NotNull
    private String originalFilename;
    @NotNull
    private Long fileSize;
    @NotNull
    private VoiceNoteStatus status;
    private Long noteId;
    @NotNull
    private Instant createdDate;
    @NotNull
    private Instant updatedDate;
}