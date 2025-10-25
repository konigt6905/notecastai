package com.notecastai.tag.api.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TagDTO {
    Long id;
    Long userId;
    String name;
}
