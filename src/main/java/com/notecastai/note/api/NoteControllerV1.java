package com.notecastai.note.api;

import com.notecastai.note.api.dto.*;
import com.notecastai.note.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
@Validated
public class NoteControllerV1 {

    private final NoteService noteService;

    @PostMapping
    public NoteDTO create(@Valid @RequestBody NoteCreateRequest request) {
        return noteService.create(request);
    }

    @PutMapping("/{id}/knowledge/manual")
    public NoteDTO update(@PathVariable Long id, @Valid @RequestBody NoteAdjustManualRequest request) {
        return noteService.updateManual(id, request);
    }

    @GetMapping
    public Page<NoteDTO> findAll(@Valid @ModelAttribute NotesQueryParam params,
                                 @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return noteService.findAll(params, pageable);
    }

    @GetMapping
    public Page<NoteShortDTO> findAllShort(@Valid @ModelAttribute NotesQueryParam params,
                                           @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return noteService.findAllShort(params, pageable);
    }

    @GetMapping("/{id}")
    public NoteDTO getById(@PathVariable Long id) {
        return noteService.getById(id);
    }

    @PutMapping("/{id}/knowledge/formate")
    public NoteDTO formateNoteKnowledgeBase(@PathVariable Long id,
                                            @Valid @RequestBody NoteFormatRequest request) {
        return noteService.formateNoteKnowledgeBase(id, request);
    }

    @PostMapping("/{noteId}/ask")
    public ResponseEntity<NoteQuestionResponse> askQuestion(
            @PathVariable Long noteId,
            @Valid @RequestBody NoteQuestionRequest request
    ) {
        NoteQuestionResponse response = noteService.askQuestion(noteId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/formats")
    public List<NoteFormatTypeDTO> listFormats() {
        return noteService.listFormats();
    }
}