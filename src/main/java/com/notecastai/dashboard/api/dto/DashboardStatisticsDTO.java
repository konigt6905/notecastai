package com.notecastai.dashboard.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dashboard statistics with current counts and trends versus last week")
public class DashboardStatisticsDTO {

    @Schema(description = "Total number of notes for current user", example = "25")
    private Long notesCount;

    @Schema(description = "Percentage change in notes versus last week", example = "15.5")
    private BigDecimal notesTrend;

    @Schema(description = "Total number of voice notes for current user", example = "10")
    private Long voiceNotesCount;

    @Schema(description = "Percentage change in voice notes versus last week", example = "-5.0")
    private BigDecimal voiceNotesTrend;

    @Schema(description = "Total number of notecasts for current user", example = "3")
    private Long notecastsCount;

    @Schema(description = "Percentage change in notecasts versus last week", example = "50.0")
    private BigDecimal notecastsTrend;
}