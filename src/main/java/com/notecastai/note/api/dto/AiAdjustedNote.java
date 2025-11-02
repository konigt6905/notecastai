package com.notecastai.note.api.dto;

import com.notecastai.tag.api.dto.TagDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAdjustedNote {

    private String title;
    private String knowledgeBase;
    private String formattedNote;
    private List<TagDTO> tags;
    private List<AiActionDTO> proposedAiActions;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AiActionDTO {
        private String name;
        private String prompt;
    }
}