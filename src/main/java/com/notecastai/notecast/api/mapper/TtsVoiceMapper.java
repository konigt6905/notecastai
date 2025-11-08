package com.notecastai.notecast.api.mapper;

import com.notecastai.notecast.api.dto.TtsVoiceDTO;
import com.notecastai.notecast.domain.TtsVoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TtsVoiceMapper {

    private static final String VOICE_PREVIEW_URL_TEMPLATE = "/api/v1/voices/preview/%s";

    public TtsVoiceDTO toDto(TtsVoice voice) {
        return TtsVoiceDTO.builder()
                .id(voice.getId())
                .enumName(voice.name())
                .name(voice.getName())
                .language(voice.getLanguage())
                .gender(voice.getGender())
                .description(voice.getDescription())
                .previewUrl(String.format(VOICE_PREVIEW_URL_TEMPLATE, voice.getId()))
                .build();
    }

    public List<TtsVoiceDTO> toDtoList(TtsVoice[] voices) {
        return Arrays.stream(voices)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TtsVoiceDTO> getAllVoices() {
        return toDtoList(TtsVoice.values());
    }

}