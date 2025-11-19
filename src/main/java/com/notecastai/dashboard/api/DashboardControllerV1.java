package com.notecastai.dashboard.api;

import com.notecastai.dashboard.api.dto.DashboardStatisticsDTO;
import com.notecastai.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "Dashboard statistics and analytics")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Validated
@Slf4j
public class DashboardControllerV1 {

    private final DashboardService dashboardService;

    @Operation(
            summary = "Get dashboard statistics",
            description = "Get dashboard statistics for the current authenticated user including counts and weekly trends"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DashboardStatisticsDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            )
    })
    @GetMapping("/statistics")
    public DashboardStatisticsDTO getStatistics() {
        log.debug("Getting dashboard statistics for current user");
        DashboardStatisticsDTO statistics = dashboardService.getStatistics();
        log.debug("Retrieved dashboard statistics: notes={}, voiceNotes={}, notecasts={}",
                statistics.getNotesCount(),
                statistics.getVoiceNotesCount(),
                statistics.getNotecastsCount());
        return statistics;
    }
}