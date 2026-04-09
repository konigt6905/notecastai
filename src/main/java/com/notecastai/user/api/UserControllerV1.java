package com.notecastai.user.api;

import com.notecastai.user.api.dto.UserCreateRequest;
import com.notecastai.user.api.dto.UserDTO;
import com.notecastai.user.api.dto.UserUpdateRequest;
import com.notecastai.user.api.mapper.UserCommandMapper;
import com.notecastai.user.service.UserService;
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

@Tag(name = "Users", description = "User management endpoints")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserControllerV1 {

    private final UserService userService;
    private final UserCommandMapper commandMapper;

    @Operation(
            summary = "Create user",
            description = "Register a new user in the system"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserCreateRequest request) {
        return userService.create(commandMapper.toCommand(request));
    }

    @Operation(
            summary = "Get current user",
            description = "Retrieve the currently authenticated user's profile"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/me")
    public UserDTO me() {
        return userService.getCurrentUser();
    }

    @Operation(
            summary = "Update current user preferences",
            description = "Update the current user's default format, preferred voice, and preferred language settings"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    })
    @PutMapping("/me")
    public UserDTO update(@Valid @RequestBody UserUpdateRequest request) {
        return userService.updateCurrentUser(commandMapper.toCommand(request));
    }

    @Operation(
            summary = "Get user by Clerk ID",
            description = "Retrieve user details using Clerk authentication user ID. Only returns the caller's own data."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/by-clerk/{clerkUserId}")
    public UserDTO getByClerk(
            @Parameter(description = "Clerk user ID", required = true)
            @PathVariable String clerkUserId
    ) {
        return userService.getByClerkUserIdForCurrentUser(clerkUserId);
    }
}