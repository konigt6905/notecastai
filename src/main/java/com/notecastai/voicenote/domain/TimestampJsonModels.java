package com.notecastai.voicenote.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * JSON models for serializing timestamps to database
 */
public class TimestampJsonModels {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WordTimestampJson {
        private String word;
        private Double startTime;
        private Double endTime;
        private Integer sequence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SegmentTimestampJson {
        private Integer id;
        private String text;
        private Double startTime;
        private Double endTime;
        private Double avgLogProb;
        private Double compressionRatio;
        private Double noSpeechProb;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WordTimestampsWrapper {
        private List<WordTimestampJson> words;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SegmentTimestampsWrapper {
        private List<SegmentTimestampJson> segments;
    }
}
