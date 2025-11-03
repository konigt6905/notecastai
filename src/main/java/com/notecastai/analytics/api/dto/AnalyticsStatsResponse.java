package com.notecastai.analytics.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsStatsResponse {

    private Long notesCreated;
    private Long voiceNotesProcessed;
    private Long notecastsGenerated;
    private TrendData trends;
    private List<TopTagDTO> topTags;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrendData {
        private Long notesThisWeek;
        private Long notesLastWeek;
        private Double growth; // Percentage
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopTagDTO {
        private Long id;
        private String name;
        private Long count;
    }
}