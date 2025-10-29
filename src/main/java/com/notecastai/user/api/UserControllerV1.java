package com.notecastai.user.api;

import com.notecastai.user.api.dto.UserCreateRequest;
import com.notecastai.user.api.dto.UserDTO;
import com.notecastai.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserControllerV1 {

    private final UserService userService;

    @PostMapping
    public UserDTO create(@Valid @RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    @GetMapping("/{id}")
    public UserDTO get(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping
    public Page<UserDTO> list(@PageableDefault Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/by-clerk/{clerkUserId}")
    public UserDTO getByClerk(@PathVariable String clerkUserId) {
        return userService.getByClerkUserId(clerkUserId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}