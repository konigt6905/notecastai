package com.notecastai.notecast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteCastShareResponse {
    private String shareUrl;
    private Instant expiresAt;
    private String shareToken;
}