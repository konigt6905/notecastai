package com.notecastai.voicenote.api;

import com.notecastai.voicenote.api.dto.*;
import com.notecastai.voicenote.service.VoiceNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/voice-notes")
@RequiredArgsConstructor
public class VoiceNoteControllerV1 {

    private final VoiceNoteService voiceNoteService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadVoiceNoteResponse upload(
            @Valid @ModelAttribute VoiceNoteCreateRequest request
    ) {
        return voiceNoteService.upload(request);
    }

    @GetMapping("/{id}")
    public VoiceNoteDTO getById(@PathVariable Long id) {
        return voiceNoteService.getById(id);
    }

    @GetMapping
    public Page<VoiceNoteDTO> findAll(
            @ModelAttribute VoiceNoteQueryParam params,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return voiceNoteService.findAll(params, pageable);
    }

    @GetMapping("/short")
    public Page<VoiceNoteShortDTO> findAllShort(
            @ModelAttribute VoiceNoteQueryParam params,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return voiceNoteService.findAllShort(params, pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        voiceNoteService.deactivate(id);
    }
}
