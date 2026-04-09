package com.notecastai.tag.api;

import com.notecastai.tag.api.dto.TagCreateRequest;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.api.dto.TagUpdateRequest;
import com.notecastai.tag.api.mapper.TagCommandMapper;
import com.notecastai.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final TagCommandMapper commandMapper;

    @Operation(
            summary = "Create tag",
            description = "Create a new tag for organizing notes"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tag created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagDTO create(@Valid @RequestBody TagCreateRequest request) {
        return tagService.create(commandMapper.toCommand(request));
    }

    @Operation(
            summary = "Update tag",
            description = "Update an existing tag (rename)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or tag name already exists", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tag not found", content = @Content)
    })
    @PutMapping("/{id}")
    public TagDTO update(
            @Parameter(description = "Tag ID to update", required = true)
            @PathVariable Long id,
            @Valid @RequestBody TagUpdateRequest request
    ) {
        return tagService.update(id, commandMapper.toCommand(request));
    }

    @Operation(
            summary = "List user tags",
            description = "Get all tags belonging to the current user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tags retrieved successfully")
    })
    @GetMapping
    public List<TagDTO> list() {
        return tagService.findAllByCurrentUser();
    }

    @Operation(
            summary = "Get tag by ID",
            description = "Retrieve a specific tag by ID for the current user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag found"),
            @ApiResponse(responseCode = "404", description = "Tag not found", content = @Content)
    })
    @GetMapping("/{id}")
    public TagDTO get(
            @Parameter(description = "Tag ID", required = true)
            @PathVariable Long id
    ) {
        return tagService.getForCurrentUser(id);
    }

    @Operation(
            summary = "Delete tag",
            description = "Delete a tag for the current user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tag not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Tag ID to delete", required = true)
            @PathVariable Long id
    ) {
        tagService.deleteForCurrentUser(id);
    }
}
