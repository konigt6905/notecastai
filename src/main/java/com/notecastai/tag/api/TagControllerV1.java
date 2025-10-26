package com.notecastai.tag.api;

import com.notecastai.tag.api.dto.TagCreateRequest;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Validated
public class TagControllerV1 {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagDTO> create(@Valid @RequestBody TagCreateRequest request) {
        TagDTO created = tagService.create(request);
        return ResponseEntity.created(URI.create("/api/tags/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TagDTO>> list(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(tagService.findAllByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> get(@PathVariable Long id, @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(tagService.getForUser(id, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id, @RequestParam("userId") Long userId) {
        tagService.deactivateForUser(id, userId);
        return ResponseEntity.noContent().build();
    }

}