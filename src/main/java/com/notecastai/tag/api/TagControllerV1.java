package com.notecastai.tag.api;

import com.notecastai.tag.api.dto.TagCreateRequest;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.api.dto.TagUpdateRequest;
import com.notecastai.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tags", description = "Tag management for organizing notes")
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Validated
public class TagControllerV1 {

    private final TagService tagService;

    @Operation(
            summary = "Create tag",
            description = "Create a new tag for organizing notes"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    public TagDTO create(@Valid @RequestBody TagCreateRequest request) {
        return tagService.create(request);
    }

    @Operation(
            summary = "Update tag",
            description = "Update an existing tag (rename)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or tag name already exists", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tag not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PutMapping("/{id}")
    public TagDTO update(
            @Parameter(description = "Tag ID to update", required = true)
            @PathVariable Long id,
            @Valid @RequestBody TagUpdateRequest request
    ) {
        return tagService.update(id, request);
    }

    @Operation(
            summary = "List user tags",
            description = "Get all tags belonging to a specific user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tags retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    public List<TagDTO> list(
            @Parameter(description = "User ID to retrieve tags for", required = true)
            @RequestParam("userId") Long userId
    ) {
        return tagService.findAllByUser(userId);
    }

    @Operation(
            summary = "Get tag by ID",
            description = "Retrieve a specific tag by ID for a given user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag found"),
            @ApiResponse(responseCode = "404", description = "Tag not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public TagDTO get(
            @Parameter(description = "Tag ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "User ID", required = true)
            @RequestParam("userId") Long userId
    ) {
        return tagService.getForUser(id, userId);
    }

    @Operation(
            summary = "Delete tag",
            description = "Delete a tag for a specific user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tag not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "Tag ID to delete", required = true)
            @PathVariable Long id,
            @Parameter(description = "User ID", required = true)
            @RequestParam("userId") Long userId
    ) {
        tagService.deleteForUser(id, userId);
    }
}