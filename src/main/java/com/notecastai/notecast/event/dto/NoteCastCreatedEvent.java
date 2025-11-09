package com.notecastai.notecast.event.dto;

import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.domain.TranscriptSize;
import com.notecastai.notecast.domain.TtsVoice;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NoteCastCreatedEvent extends ApplicationEvent {

    private final Long noteCastId;
    private final String noteContent;
    private final NoteCastStyle style;
    private final String customInstructions;
    private final TranscriptSize size;
    private final TtsVoice voice;

    public NoteCastCreatedEvent(Object source,
                                Long noteCastId,
                                String noteContent,
                                NoteCastStyle style,
                                TranscriptSize size,
                                String customInstructions,
                                TtsVoice voice) {
        super(source);
        this.noteCastId = noteCastId;
        this.noteContent = noteContent;
        this.style = style;
        this.size = size;
        this.customInstructions = customInstructions;
        this.voice = voice;
    }
}
