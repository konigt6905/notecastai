package com.notecastai.notecast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteCastTimestampsDTO {

    private Long noteCastId;
    private List<WordTimestampDTO> words;
    private List<SegmentTimestampDTO> segments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WordTimestampDTO {
        private String word;
        private Double startTime;
        private Double endTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SegmentTimestampDTO {
        private Integer id;
        private String text;
        private Double startTime;
        private Double endTime;
        private Double avgLogProb;
        private Double compressionRatio;
        private Double noSpeechProb;
    }
}