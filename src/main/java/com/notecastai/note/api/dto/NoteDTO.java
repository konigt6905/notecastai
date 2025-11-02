package com.notecastai.note.api.dto;

import com.notecastai.note.domain.FormateType;
import com.notecastai.note.domain.NoteType;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.user.api.dto.UserDTO;
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
public class NoteDTO {

    @NotNull
    private Long id;
    @NotNull
    private UserDTO user;
    @NotNull
    private String title;
    @NotNull
    private String knowledgeBase;
    @NotNull
    private String formattedNote;
    @NotNull
    private NoteType type;
    @NotNull
    private FormateType currentFormate;
    @NotNull
    private List<TagDTO> tags;
    @NotNull
    private Instant createdDate;
    @NotNull
    private Instant updatedDate;
    private List<AiActionDto> proposedAiActions;
    @NotNull
    private Boolean inactive;

}