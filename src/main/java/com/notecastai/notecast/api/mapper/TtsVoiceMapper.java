package com.notecastai.notecast.api.mapper;

import com.notecastai.common.exeption.BusinessException;
import com.notecastai.notecast.api.dto.TtsVoiceDTO;
import com.notecastai.notecast.domain.TtsVoice;
import com.notecastai.integration.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.INTERNAL_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class TtsVoiceMapper {

    private final StorageService storageService;

    private static final Duration PRESIGNED_URL_TTL = Duration.ofHours(24);

    public TtsVoiceDTO toDto(TtsVoice voice) {
        String previewUrl = generatePresignedUrl(voice.getS3SamplePath());

        return TtsVoiceDTO.builder()
                .id(voice.getId())
                .name(voice.getName())
                .language(voice.getLanguage())
                .gender(voice.getGender())
                .description(voice.getDescription())
                .previewUrl(previewUrl)
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

    private String generatePresignedUrl(String s3Key) {
        try {
            URI presignedUri = storageService.presignedGet(s3Key, PRESIGNED_URL_TTL);
            return presignedUri.toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for S3 key: {}", s3Key, e);
            throw BusinessException.of(
                INTERNAL_ERROR.append(" Failed to generate presigned URL for voice sample: " + s3Key),
                e
            );
        }
    }
}