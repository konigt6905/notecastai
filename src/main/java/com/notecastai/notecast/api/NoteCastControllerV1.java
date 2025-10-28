package com.notecastai.notecast.api;

import com.notecastai.notecast.api.dto.*;
import com.notecastai.notecast.service.NoteCastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notecasts")
@RequiredArgsConstructor
public class NoteCastControllerV1 {

    private final NoteCastService noteCastService;

    @PostMapping
    public NoteCastResponseDTO create(@Valid @RequestBody NoteCastCreateRequest request) {
        return noteCastService.create(request);
    }

    @GetMapping("/{id}")
    public NoteCastResponseDTO getById(@PathVariable Long id) {
        return noteCastService.getById(id);
    }

    @GetMapping
    public Page<NoteCastResponseDTO> findAll(
            @ModelAttribute NoteCastQueryParam params,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return noteCastService.findAll(params, pageable);
    }

    @GetMapping("/short")
    public Page<NoteCastShortDTO> findAllShort(
            @ModelAttribute NoteCastQueryParam params,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return noteCastService.findAllShort(params, pageable);
    }

    @GetMapping("/styles")
    public List<NoteCastStyleDTO> listStyles() {
        return noteCastService.listStyles();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        noteCastService.deactivate(id);
    }

}