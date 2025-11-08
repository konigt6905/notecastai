package com.notecastai.voicenote.api.dto;


import com.notecastai.note.api.dto.NoteDTO;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.voicenote.domain.VoiceNoteStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@With
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
    private NoteDTO note;
    private String transcript;
    private TranscriptionLanguage language;
    private Integer durationSeconds;
    private List<TagDTO> tags;
    @NotNull
    private Instant createdDate;
    @NotNull
    private Instant updatedDate;

}