package com.notecastai.analytics.api;

import com.notecastai.analytics.api.dto.AnalyticsPeriod;
import com.notecastai.analytics.api.dto.AnalyticsStatsResponse;
import com.notecastai.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Analytics", description = "User analytics and statistics")
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Validated
public class AnalyticsControllerV1 {

    private final AnalyticsService analyticsService;

    @Operation(
            summary = "Get analytics stats",
            description = "Get analytics statistics for a user over a specified period"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stats retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping("/stats")
    public AnalyticsStatsResponse getStats(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "Analytics period (WEEK, MONTH, YEAR, ALL)", required = false)
            @RequestParam(defaultValue = "MONTH") AnalyticsPeriod period
    ) {
        return analyticsService.getStats(userId, period);
    }
}