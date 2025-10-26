package com.notecastai.integration.ai.impl;

import com.notecastai.integration.ai.NoteAiEditor;
import com.notecastai.note.domain.FormateType;
import com.notecastai.tag.domain.TagEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NoteAiEditorImpl implements NoteAiEditor {

    @Override
    public String edit(String rawText, FormateType formateType, String instruction) {
        // Simple implementation that appends the instruction to the raw text.
        return rawText + "\n\nInstruction: " + instruction;
    }

    private List<TagEntity> resolveAndValidateTags(Long userId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return List.of();
        List<TagEntity> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (Long tagId : tagIds) {
            if (tagId == null || seen.contains(tagId)) continue;
            TagEntity tag = tagRepository.findByIdAndUserOrThrow(tagId, userId);
            result.add(tag);
            seen.add(tagId);
        }
        return result;
    }
}
