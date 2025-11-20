package com.notecastai.gamenote.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class GameNoteCreatedEvent {

    private final Long gameNoteId;

}
