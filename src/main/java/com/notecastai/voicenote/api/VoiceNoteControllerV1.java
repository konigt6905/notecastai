package com.notecastai.voicenote.api;

import com.notecastai.voicenote.api.dto.*;
import com.notecastai.voicenote.service.VoiceNoteService;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Voice Notes", description = "Voice note upload and management endpoints")
@RestController
@RequestMapping("/api/v1/voice-notes")
@RequiredArgsConstructor
public class VoiceNoteControllerV1 {

    private final VoiceNoteService voiceNoteService;

    @Operation(
            summary = "Upload voice note",
            description = "Upload an audio file for processing and transcription. Supports various audio formats."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voice note uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or request parameters", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadVoiceNoteResponse upload(
            @Valid @ModelAttribute VoiceNoteCreateRequest request
    ) {
        return voiceNoteService.upload(request);
    }

    @Operation(
            summary = "Get voice note by ID",
            description = "Retrieve a specific voice note with full details including transcription and processing status"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voice note found"),
            @ApiResponse(responseCode = "404", description = "Voice note not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public VoiceNoteDTO getById(
            @Parameter(description = "Voice note ID", required = true)
            @PathVariable Long id
    ) {
        return voiceNoteService.getById(id);
    }

    @Operation(
            summary = "List all voice notes",
            description = "Get paginated list of voice notes with optional filtering"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voice notes retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    public Page<VoiceNoteDTO> findAll(
            @Parameter(description = "Query parameters for filtering")
            @ModelAttribute VoiceNoteQueryParam params,
            @Parameter(description = "Pagination parameters (default: page=0, size=20, sort=createdDate,desc)")
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return voiceNoteService.findAll(params, pageable);
    }

    @Operation(
            summary = "List voice notes (short format)",
            description = "Get paginated list of voice notes with minimal details for list views"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voice notes retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/short")
    public Page<VoiceNoteShortDTO> findAllShort(
            @Parameter(description = "Query parameters for filtering")
            @ModelAttribute VoiceNoteQueryParam params,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return voiceNoteService.findAllShort(params, pageable);
    }

    @Operation(
            summary = "Delete voice note",
            description = "Permanently delete a voice note and its associated audio file"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Voice note deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Voice note not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Voice note ID to delete", required = true)
            @PathVariable Long id
    ) {
        voiceNoteService.delete(id);
    }

    @Operation(
            summary = "Add tag to voice note",
            description = "Associate a tag with a specific voice note"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag added successfully"),
            @ApiResponse(responseCode = "404", description = "Voice note or tag not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PutMapping("/{voiceNoteId}/tags/{tagId}")
    public VoiceNoteDTO addTag(
            @Parameter(description = "Voice note ID", required = true)
            @PathVariable Long voiceNoteId,
            @Parameter(description = "Tag ID to add", required = true)
            @PathVariable Long tagId
    ) {
        return voiceNoteService.addTag(voiceNoteId, tagId);
    }

    @Operation(
            summary = "Remove tag from voice note",
            description = "Remove a tag association from a specific voice note"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag removed successfully"),
            @ApiResponse(responseCode = "404", description = "Voice note not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{voiceNoteId}/tags/{tagId}")
    public VoiceNoteDTO removeTag(
            @Parameter(description = "Voice note ID", required = true)
            @PathVariable Long voiceNoteId,
            @Parameter(description = "Tag ID to remove", required = true)
            @PathVariable Long tagId
    ) {
        return voiceNoteService.removeTag(voiceNoteId, tagId);
    }
}