package com.notecastai.note.api.dto;

import com.notecastai.note.domain.FormateType;
import com.notecastai.note.domain.NoteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotesQueryParam {

    private List<Long> tagIds;

    private String titleLike;

    private NoteType type;

    private FormateType currentFormate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant to;
}