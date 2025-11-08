package com.notecastai.common.util;

import com.notecastai.common.exeption.BusinessException;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.INVALID_REQUEST;

@UtilityClass
public class FileValidationUtil {

    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of(
            "audio/mpeg",           // .mp3
            "audio/mp4",            // .m4a
            "audio/x-m4a",          // .m4a
            "audio/wav",            // .wav
            "audio/wave",           // .wav
            "audio/x-wav",          // .wav
            "audio/webm",           // .webm
            "audio/ogg",            // .ogg
            "audio/flac",           // .flac
            "audio/x-flac"          // .flac
    );

    private static final long MAX_FILE_SIZE = 30 * 1024 * 1024; // 30 MB

    public static void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.of(INVALID_REQUEST.append(" File is required"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AUDIO_TYPES.contains(contentType.toLowerCase())) {
            throw BusinessException.of(INVALID_REQUEST.append(
                    " Invalid file type. Allowed types: MP3, WAV, M4A, WEBM, OGG, FLAC. Used type: " + contentType
            ));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw BusinessException.of(INVALID_REQUEST.append(
                    " File size exceeds maximum allowed size of 30 MB"
            ));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw BusinessException.of(INVALID_REQUEST.append(" Filename is required"));
        }
    }
}
