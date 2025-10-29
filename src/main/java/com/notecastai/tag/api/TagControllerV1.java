package com.notecastai.tag.api;

import com.notecastai.tag.api.dto.TagCreateRequest;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Validated
public class TagControllerV1 {

    private final TagService tagService;

    @PostMapping
    public TagDTO create(@Valid @RequestBody TagCreateRequest request) {
        return tagService.create(request);
    }

    @GetMapping
    public List<TagDTO> list(@RequestParam("userId") Long userId) {
        return tagService.findAllByUser(userId);
    }

    @GetMapping("/{id}")
    public TagDTO get(@PathVariable Long id, @RequestParam("userId") Long userId) {
        return tagService.getForUser(id, userId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestParam("userId") Long userId) {
        tagService.deleteForUser(id, userId);
    }

}