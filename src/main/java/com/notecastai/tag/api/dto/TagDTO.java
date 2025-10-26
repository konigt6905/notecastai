package com.notecastai.tag.api.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TagDTO {
    private Long id;
    private String name;
}
