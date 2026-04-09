package com.notecastai.integration.ai.provider.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class OpenAiTranscriptionResponse {

    private String task;
    private String language;
    private Double duration;
    private String text;
    private List<Segment> segments;
    private List<Word> words;

    @JsonIgnore
    private String requestId;

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
}
