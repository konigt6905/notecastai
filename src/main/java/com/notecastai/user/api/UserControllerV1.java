package com.notecastai.user.api;

import com.notecastai.user.api.dto.UserCreateRequest;
import com.notecastai.user.api.dto.UserDTO;
import com.notecastai.user.api.dto.UserUpdateRequest;
import com.notecastai.user.service.UserService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "User management endpoints")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserControllerV1 {

    private final UserService userService;

    @Operation(
            summary = "Create user",
            description = "Register a new user in the system"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    })
    @PostMapping
    public UserDTO create(@Valid @RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    @Operation(
            summary = "Update user preferences",
            description = "Update user's default format, preferred voice, and preferred language settings"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PutMapping("/{id}")
    public UserDTO update(
            @Parameter(description = "User ID to update", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return userService.update(id, request);
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieve user details by internal user ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
    })
    @GetMapping("/{id}")
    public UserDTO get(
            @Parameter(description = "Internal user ID", required = true)
            @PathVariable Long id
    ) {
        return userService.getById(id);
    }

    @Operation(
            summary = "List all users",
            description = "Get paginated list of all users in the system"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
    @GetMapping
    public Page<UserDTO> list(
            @Parameter(description = "Pagination parameters")
            @PageableDefault Pageable pageable
    ) {
        return userService.findAll(pageable);
    }

    @Operation(
            summary = "Get user by Clerk ID",
            description = "Retrieve user details using Clerk authentication user ID"
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
        return userService.getByClerkUserId(clerkUserId);
    }

    @Operation(
            summary = "Delete user",
            description = "Permanently delete a user from the system"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "User ID to delete", required = true)
            @PathVariable Long id
    ) {
        userService.delete(id);
    }
}