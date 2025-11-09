package com.notecastai.notecast.event.dto;

import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.domain.TranscriptSize;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NoteCastCreatedEvent extends ApplicationEvent {

    private final Long noteCastId;
    private final String noteContent;
    private final NoteCastStyle style;
    private final String customInstructions;
    private final TranscriptSize size;

    public NoteCastCreatedEvent(Object source, Long noteCastId, String noteContent, NoteCastStyle style, TranscriptSize size, String customInstructions) {
        super(source);
        this.noteCastId = noteCastId;
        this.noteContent = noteContent;
        this.style = style;
        this.size = size;
        this.customInstructions = customInstructions;
    }
}