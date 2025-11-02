package com.notecastai.note.api;

import com.notecastai.note.api.dto.*;
import com.notecastai.note.service.NoteService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notes", description = "AI-powered Notes management")
@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
@Validated
public class NoteControllerV1 {

    private final NoteService noteService;

    @Operation(
            summary = "Create note",
            description = "Create a new note in the system"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    })
    @PostMapping
    public NoteDTO create(@Valid @RequestBody CreateNoteRequest request) {
        return noteService.create(request);
    }

    @Operation(
            summary = "Combine notes",
            description = "Create a new COMBINED type note by merging knowledge bases from multiple notes (max 30)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Combined note created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or note limit exceeded", content = @Content),
            @ApiResponse(responseCode = "404", description = "One or more notes not found", content = @Content)
    })
    @PostMapping("/combine")
    public NoteDTO combine(@Valid @RequestBody NoteCombineRequest request) {
        return noteService.combine(request);
    }

    @Operation(
            summary = "Update note knowledge (manual)",
            description = "Manually update the knowledge base content of a note"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note updated successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    })
    @PutMapping("/{id}/knowledge/manual")
    public NoteDTO update(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody NoteAdjustManualRequest request
    ) {
        return noteService.updateManual(id, request);
    }

    @Operation(
            summary = "List all notes",
            description = "Get paginated list of notes with optional filtering"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notes retrieved successfully")
    })
    @GetMapping
    public Page<NoteDTO> findAll(
            @Valid @ModelAttribute NotesQueryParam params,
            @Parameter(description = "Pagination parameters (default: page=0, size=20, sort=createdDate,desc)")
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return noteService.findAll(params, pageable);
    }

    @Operation(
            summary = "List notes (short format)",
            description = "Get paginated list of notes with minimal details for list views"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notes retrieved successfully")
    })
    @GetMapping("/short")
    public Page<NoteShortDTO> findAllShort(
            @Valid @ModelAttribute NotesQueryParam params,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return noteService.findAllShort(params, pageable);
    }

    @Operation(
            summary = "Get note by ID",
            description = "Retrieve a specific note with full details"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note found"),
            @ApiResponse(responseCode = "404", description = "Note not found", content = @Content)
    })
    @GetMapping("/{id}")
    public NoteDTO getById(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long id
    ) {
        return noteService.getById(id);
    }

    @Operation(
            summary = "Format note knowledge base",
            description = "AI-format the note's knowledge base into a specific format (summary, bullet points, etc.)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note formatted successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid format request", content = @Content)
    })
    @PutMapping("/{id}/knowledge/format")
    public NoteDTO formateNoteKnowledgeBase(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody NoteKnowledgeFormatRequest request
    ) {
        return noteService.formateNoteKnowledgeBase(id, request);
    }

    @Operation(
            summary = "Adjust note with AI",
            description = "AI-adjust the note by formatting the content and optionally updating title and tags"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note adjusted successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    })
    @PutMapping("/{id}/formate")
    public NoteDTO adjustNote(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody FormateNoteRequest request
    ) {
        return noteService.formateNote(id, request);
    }

    @Operation(
            summary = "Ask question about note",
            description = "Use AI to answer questions about the content of a specific note"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question answered successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid question", content = @Content)
    })
    @PostMapping("/{noteId}/ask")
    public NoteQuestionResponse askQuestion(
            @Parameter(description = "Note ID to query", required = true)
            @PathVariable Long noteId,
            @Valid @RequestBody NoteQuestionRequest request
    ) {
        return noteService.askQuestion(noteId, request);
    }

    @Operation(
            summary = "List available formats",
            description = "Get all available note formatting types"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Formats retrieved successfully")
    })
    @GetMapping("/formats")
    public List<NoteFormatTypeDTO> listFormats() {
        return noteService.listFormats();
    }

    @Operation(
            summary = "Add tag to note",
            description = "Associate a tag with a specific note"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag added successfully"),
            @ApiResponse(responseCode = "404", description = "Note or tag not found", content = @Content)
    })
    @PutMapping("/{noteId}/tags/{tagId}")
    public NoteDTO addTag(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long noteId,
            @Parameter(description = "Tag ID to add", required = true)
            @PathVariable Long tagId
    ) {
        return noteService.addTag(noteId, tagId);
    }

    @Operation(
            summary = "Remove tag from note",
            description = "Remove a tag association from a specific note"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag removed successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found", content = @Content)
    })
    @DeleteMapping("/{noteId}/tags/{tagId}")
    public NoteDTO removeTag(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long noteId,
            @Parameter(description = "Tag ID to remove", required = true)
            @PathVariable Long tagId
    ) {
        return noteService.removeTag(noteId, tagId);
    }
}