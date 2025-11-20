package com.notecastai.gamenote.api;

import com.notecastai.gamenote.api.dto.*;
import com.notecastai.gamenote.service.GameNoteService;
import com.notecastai.gamenote.service.GameNoteStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Game Notes", description = "AI-powered quiz and flashcard generation from notes")
@RestController
@RequestMapping("/api/v1/game-notes")
@RequiredArgsConstructor
@Validated
public class GameNoteControllerV1 {

    private final GameNoteService gameNoteService;
    private final GameNoteStatisticsService statisticsService;

    @Operation(
            summary = "Create game note",
            description = "Create a new game note with AI-generated questions from a source note. " +
                    "Processing is asynchronous - poll the GET endpoint to check status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Game note creation initiated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Source note not found", content = @Content),
            @ApiResponse(responseCode = "429", description = "User game note limit exceeded", content = @Content)
    })
    @PostMapping
    public CreateGameNoteResponse create(@Valid @RequestBody GameNoteCreateRequest request) {
        return gameNoteService.create(request);
    }

    @Operation(
            summary = "Get game note by ID",
            description = "Retrieve a game note with all generated questions. " +
                    "Status can be: PENDING, PROCESSING, PROCESSED, FAILED"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Game note retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Game note not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Game note does not belong to user", content = @Content)
    })
    @GetMapping("/{id}")
    public GameNoteDTO getById(
            @Parameter(description = "Game note ID", required = true)
            @PathVariable Long id
    ) {
        return gameNoteService.getById(id);
    }

    @Operation(
            summary = "List all game notes",
            description = "Get paginated list of game notes with optional filtering by status, question type, difficulty, etc."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Game notes retrieved successfully")
    })
    @GetMapping
    public Page<GameNoteShortDTO> findAll(
            @Valid @ModelAttribute GameNoteQueryParam params,
            @Parameter(description = "Pagination parameters (default: page=0, size=20, sort=createdDate,desc)")
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return gameNoteService.findAll(params, pageable);
    }

    @Operation(
            summary = "Delete game note",
            description = "Soft delete a game note (sets inactive flag)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Game note deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Game note not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Game note does not belong to user", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Game note ID", required = true)
            @PathVariable Long id
    ) {
        gameNoteService.delete(id);
    }

    @Operation(
            summary = "Add tag to game note",
            description = "Associate a tag with a game note"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag added successfully"),
            @ApiResponse(responseCode = "404", description = "Game note or tag not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Game note or tag does not belong to user", content = @Content)
    })
    @PutMapping("/{id}/tags/{tagId}")
    public GameNoteDTO addTag(
            @Parameter(description = "Game note ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Tag ID", required = true)
            @PathVariable Long tagId
    ) {
        return gameNoteService.addTag(id, tagId);
    }

    @Operation(
            summary = "Remove tag from game note",
            description = "Disassociate a tag from a game note"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag removed successfully"),
            @ApiResponse(responseCode = "404", description = "Game note not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Game note does not belong to user", content = @Content)
    })
    @DeleteMapping("/{id}/tags/{tagId}")
    public GameNoteDTO removeTag(
            @Parameter(description = "Game note ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Tag ID", required = true)
            @PathVariable Long tagId
    ) {
        return gameNoteService.removeTag(id, tagId);
    }

    // ========== Statistics Endpoints ==========

    @Operation(
            summary = "Submit game note statistics",
            description = "Submit performance statistics after completing a game note session. " +
                    "Creates a new statistics record with auto-incremented attempt number. " +
                    "Score is calculated based on correctness (70%), speed (20%), and completion (10%)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid statistics data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Game note not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Game note does not belong to user", content = @Content)
    })
    @PostMapping("/{id}/statistics")
    public GameNoteStatisticsDTO submitStatistics(
            @Parameter(description = "Game note ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody SubmitStatisticsRequest request
    ) {
        return statisticsService.submitStatistics(id, request);
    }

    @Operation(
            summary = "Get all attempts for a game note",
            description = "Retrieve all statistics records for a specific game note, ordered by attempt number (latest first)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Game note not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Game note does not belong to user", content = @Content)
    })
    @GetMapping("/{id}/statistics")
    public List<GameNoteStatisticsDTO> getAllAttempts(
            @Parameter(description = "Game note ID", required = true)
            @PathVariable Long id
    ) {
        return statisticsService.getAllAttempts(id);
    }

    @Operation(
            summary = "Get latest attempt for a game note",
            description = "Retrieve the most recent statistics record for a specific game note"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Latest statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Game note not found or no attempts found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Game note does not belong to user", content = @Content)
    })
    @GetMapping("/{id}/statistics/latest")
    public GameNoteStatisticsDTO getLatestAttempt(
            @Parameter(description = "Game note ID", required = true)
            @PathVariable Long id
    ) {
        return statisticsService.getLatestAttempt(id);
    }

    @Operation(
            summary = "Get best attempt for a game note",
            description = "Retrieve the highest scoring statistics record for a specific game note"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Best statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Game note not found or no attempts found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Game note does not belong to user", content = @Content)
    })
    @GetMapping("/{id}/statistics/best")
    public GameNoteStatisticsDTO getBestAttempt(
            @Parameter(description = "Game note ID", required = true)
            @PathVariable Long id
    ) {
        return statisticsService.getBestAttempt(id);
    }

    @Operation(
            summary = "Get game note summary",
            description = "Get aggregated statistics summary for a specific game note including best score, averages, and mastery level"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Game note not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Game note does not belong to user", content = @Content)
    })
    @GetMapping("/{id}/statistics/summary")
    public GameNoteStatisticsSummaryDTO getGameNoteSummary(
            @Parameter(description = "Game note ID", required = true)
            @PathVariable Long id
    ) {
        return statisticsService.getGameNoteSummary(id);
    }

    @Operation(
            summary = "Get user statistics summary",
            description = "Get aggregated statistics across all game notes for the current user, " +
                    "including overall progress, best scores, and mastery levels"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User summary retrieved successfully")
    })
    @GetMapping("/statistics/summary")
    public List<GameNoteStatisticsSummaryDTO> getUserSummary() {
        return statisticsService.getUserSummary();
    }
}
