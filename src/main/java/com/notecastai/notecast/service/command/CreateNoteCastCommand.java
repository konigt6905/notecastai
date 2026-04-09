package com.notecastai.notecast.service.command;

import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.domain.TranscriptSize;
import com.notecastai.notecast.domain.TtsVoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoteCastCommand {
    private Long noteId;
    private NoteCastStyle style;
    private TranscriptSize size;
    private String title;
    private String customInstructions;
    private TtsVoice voice;
    private Boolean takeTagsFromNote;
    private List<Long> tagIds;
}
