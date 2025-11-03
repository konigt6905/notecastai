package com.notecastai.notecast.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TtsVoiceDTO {
    private String id;
    private String name;
    private String language;
    private String gender;
    private String description;
    private String previewUrl;
}