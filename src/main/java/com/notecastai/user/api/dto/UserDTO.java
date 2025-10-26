package com.notecastai.user.api.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserDTO {
    Long id;
    String clerkUserId;
}