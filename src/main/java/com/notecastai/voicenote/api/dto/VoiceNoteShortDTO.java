package com.notecastai.voicenote.api.dto;

import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.voicenote.domain.VoiceNoteStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

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
    private String transcript;
    private Long noteId;
    private List<TagDTO> tags;
    @NotNull
    private Instant createdDate;
    @NotNull
    private Instant updatedDate;
}