package com.notecastai.integration.ai.provider.groq.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroqTranscriptionResponse {

    private String text;

    private String language;

    private Double duration;

    private List<Segment> segments;

    private List<Word> words;

    @JsonProperty("x_groq")
    private XGroq xGroq;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Segment {

        private Integer id;
        private Integer seek;
        private Double start;
        private Double end;
        private String text;
        private List<Integer> tokens;
        private Double temperature;

        @JsonProperty("avg_logprob")
        private Double avgLogprob;

        @JsonProperty("compression_ratio")
        private Double compressionRatio;

        @JsonProperty("no_speech_prob")
        private Double noSpeechProb;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Word {

        private String word;
        private Double start;
        private Double end;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class XGroq {

        private String id;
    }
}