package com.notecastai.notecast.api;

import com.notecastai.config.TtsVoiceProperties;
import com.notecastai.notecast.api.dto.*;
import com.notecastai.notecast.api.mapper.TtsVoiceMapper;
import com.notecastai.notecast.domain.TtsVoice;
import com.notecastai.notecast.service.NoteCastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "NoteCasts", description = "NoteCast creation and management - AI-generated content from notes")
@RestController
@RequestMapping("/api/v1/notecasts")
@RequiredArgsConstructor
public class NoteCastControllerV1 {

    private final NoteCastService noteCastService;
    private final TtsVoiceMapper ttsVoiceMapper;
    private final TtsVoiceProperties ttsVoiceProperties;

    @Operation(
            summary = "Create notecast",
            description = "Generate a new notecast from selected notes using AI in a specific style"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoteCast created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    public NoteCastResponseDTO create(@Valid @RequestBody NoteCastCreateRequest request) {
        return noteCastService.create(request);
    }

    @Operation(
            summary = "Get notecast by ID",
            description = "Retrieve a specific notecast with full details"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoteCast found"),
            @ApiResponse(responseCode = "404", description = "NoteCast not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public NoteCastResponseDTO getById(
            @Parameter(description = "NoteCast ID", required = true)
            @PathVariable Long id
    ) {
        return noteCastService.getById(id);
    }

    @Operation(
            summary = "List all notecasts",
            description = "Get paginated list of notecasts with optional filtering"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoteCasts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    public Page<NoteCastResponseDTO> findAll(
            @Parameter(description = "Query parameters for filtering")
            @ModelAttribute NoteCastQueryParam params,
            @Parameter(description = "Pagination parameters (default: page=0, size=20, sort=createdDate,desc)")
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return noteCastService.findAll(params, pageable);
    }

    @Operation(
            summary = "List notecasts (short format)",
            description = "Get paginated list of notecasts with minimal details for list views"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoteCasts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/short")
    public Page<NoteCastShortDTO> findAllShort(
            @Parameter(description = "Query parameters for filtering")
            @ModelAttribute NoteCastQueryParam params,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return noteCastService.findAllShort(params, pageable);
    }

    @Operation(
            summary = "List available styles",
            description = "Get all available notecast generation styles"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Styles retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/styles")
    public List<NoteCastStyleDTO> listStyles() {
        return noteCastService.listStyles();
    }

    @Operation(
            summary = "Delete notecast",
            description = "Permanently delete a notecast"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "NoteCast deleted successfully"),
            @ApiResponse(responseCode = "404", description = "NoteCast not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "NoteCast ID to delete", required = true)
            @PathVariable Long id
    ) {
        noteCastService.delete(id);
    }

    @Operation(
            summary = "List available TTS voices",
            description = "Get all available Text-to-Speech voices for notecast generation with preview audio URLs"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voices retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to generate voice preview URLs", content = @Content)
    })
    @GetMapping("/voices")
    public List<TtsVoiceDTO> listVoices() {
        return ttsVoiceMapper.getAllVoices();
    }

    @Operation(
            summary = "Get voice preview audio",
            description = "Stream the preview audio file for a specific TTS voice"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audio file streamed successfully"),
            @ApiResponse(responseCode = "404", description = "Voice not found", content = @Content)
    })
    @GetMapping("/voices/preview/{voiceId}")
    public ResponseEntity<Resource> getVoicePreview(
            @Parameter(description = "Voice ID", required = true)
            @PathVariable String voiceId
    ) {
        TtsVoice voice = TtsVoice.fromId(voiceId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(new ClassPathResource(voice.getSampleResourcePath()));
    }

    @Operation(
            summary = "Get shareable link for notecast",
            description = "Generate a shareable public link for a notecast"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Share link generated successfully"),
            @ApiResponse(responseCode = "404", description = "NoteCast not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}/share")
    public NoteCastShareResponse share(
            @Parameter(description = "NoteCast ID", required = true)
            @PathVariable Long id
    ) {
        return noteCastService.generateShareLink(id);
    }

    @Operation(
            summary = "Add tag to notecast",
            description = "Associate a tag with a specific notecast"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag added successfully"),
            @ApiResponse(responseCode = "404", description = "NoteCast or tag not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PutMapping("/{noteCastId}/tags/{tagId}")
    public NoteCastResponseDTO addTag(
            @Parameter(description = "NoteCast ID", required = true)
            @PathVariable Long noteCastId,
            @Parameter(description = "Tag ID to add", required = true)
            @PathVariable Long tagId
    ) {
        return noteCastService.addTag(noteCastId, tagId);
    }

    @Operation(
            summary = "Remove tag from notecast",
            description = "Remove a tag association from a specific notecast"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag removed successfully"),
            @ApiResponse(responseCode = "404", description = "NoteCast not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{noteCastId}/tags/{tagId}")
    public NoteCastResponseDTO removeTag(
            @Parameter(description = "NoteCast ID", required = true)
            @PathVariable Long noteCastId,
            @Parameter(description = "Tag ID to remove", required = true)
            @PathVariable Long tagId
    ) {
        return noteCastService.removeTag(noteCastId, tagId);
    }
}
