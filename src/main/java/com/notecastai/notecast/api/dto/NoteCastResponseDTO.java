package com.notecastai.notecast.api.dto;

import com.notecastai.notecast.domain.NoteCastStatus;
import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.domain.TranscriptSize;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@With
@AllArgsConstructor
public class NoteCastResponseDTO {

    private Long id;
    private Long noteId;
    private String s3FileUrl;
    private String transcript;
    private Integer durationSeconds;
    private Long processingTimeMs;
    private TranscriptionLanguage language;
    private NoteCastStatus status;
    private NoteCastStyle style;
    private String voice;
    private TranscriptSize size;
    private List<TagDTO> tags;
    private Instant createdDate;
    private Instant updatedDate;

}