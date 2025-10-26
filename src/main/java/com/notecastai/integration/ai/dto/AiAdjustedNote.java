package com.notecastai.integration.ai.dto;

import com.notecastai.note.domain.FormateType;
import com.notecastai.tag.api.dto.TagDTO;
import lombok.Data;

import java.util.List;

@Data
public class AiAdjustedNote {

    private String title;
    private String knowledgeBase;
    private String formattedNote;
    private List<TagDTO> tags;
    private FormateType formateType;
    private String instructions;

}