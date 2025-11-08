package com.notecastai.notecast.api.mapper;

import com.notecastai.integration.storage.StorageService;
import com.notecastai.notecast.api.dto.TtsVoiceDTO;
import com.notecastai.notecast.domain.TtsVoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TtsVoiceMapper {

    private final StorageService storageService;

    public TtsVoiceDTO toDto(TtsVoice voice) {
        return TtsVoiceDTO.builder()
                .id(voice.getId())
                .name(voice.getName())
                .language(voice.getLanguage())
                .gender(voice.getGender())
                .description(voice.getDescription())
                .previewUrl(storageService.presignedGet(voice.getS3SamplePath()))
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