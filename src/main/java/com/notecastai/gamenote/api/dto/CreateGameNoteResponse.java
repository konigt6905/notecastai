package com.notecastai.gamenote.api.dto;

import com.notecastai.gamenote.domain.GameNoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateGameNoteResponse {

    private Long id;
    private GameNoteStatus status;

}
