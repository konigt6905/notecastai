package com.notecastai.tag.api.dto;

import com.notecastai.user.api.dto.UserDTO;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TagDTO {
    private UserDTO user;
    private String name;
}
