package com.notecastai.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserCreateRequest {
    @NotBlank
    String clerkUserId;
}