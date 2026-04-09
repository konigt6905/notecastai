package com.notecastai.notecast.api.mapper;

import com.notecastai.notecast.api.dto.TtsVoiceDTO;
import com.notecastai.config.TtsVoiceProperties;
import com.notecastai.notecast.domain.TtsVoice;
import com.notecastai.notecast.domain.TtsVoiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TtsVoiceMapper {

    private static final String VOICE_PREVIEW_URL_TEMPLATE = "/api/v1/notecasts/voices/preview/%s";
    private final TtsVoiceProperties ttsVoiceProperties;

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

    public List<TtsVoiceDTO> getAllVoices() {
        TtsVoiceProvider provider = ttsVoiceProperties.getVoiceProvider();
        return TtsVoice.listByProvider(provider).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

}
