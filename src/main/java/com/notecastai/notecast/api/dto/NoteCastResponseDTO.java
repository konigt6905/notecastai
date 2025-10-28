package com.notecastai.notecast.api.dto;

import com.notecastai.notecast.domain.NoteCastStatus;
import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.domain.TranscriptSize;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
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
    private TranscriptSize size;
    private Instant createdDate;
    private Instant updatedDate;

}