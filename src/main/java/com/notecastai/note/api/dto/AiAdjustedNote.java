package com.notecastai.note.api.dto;

import com.notecastai.tag.api.dto.TagDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiAdjustedNote {

    private String title;
    private String knowledgeBase;
    private String formattedNote;
    private List<TagDTO> tags;
    private List<AiActionDTO> proposedAiActions;

    @Data
    @Builder
    public static class AiActionDTO {
        private String name;
        private String prompt;
    }
}