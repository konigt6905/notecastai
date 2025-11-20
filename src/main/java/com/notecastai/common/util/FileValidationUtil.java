package com.notecastai.common.util;

import com.notecastai.common.exeption.BusinessException;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    private void debugAudioFile(MultipartFile file) {
        try {
            System.out.println("DEBUG audio contentType = " + file.getContentType());
            System.out.println("DEBUG audio originalFilename = " + file.getOriginalFilename());
            System.out.println("DEBUG audio size = " + file.getSize());

            byte[] bytes = file.getBytes();
            int len = Math.min(bytes.length, 16);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                sb.append(String.format("%02X ", bytes[i]));
            }
            System.out.println("DEBUG audio first bytes (hex): " + sb);

            // optional: dump to file so you can play it locally
            java.nio.file.Path dump = java.nio.file.Files.createTempFile("openai-debug-", ".bin");
            java.nio.file.Files.write(dump, bytes);
            System.out.println("DEBUG dumped audio to: " + dump.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MultipartFile validateAndNormalizeIfNeeded(MultipartFile file) {
        debugAudioFile(file);
        validateAudioFile(file);

        String contentType = file.getContentType() != null
                ? file.getContentType().toLowerCase()
                : "";

        if (!"audio/x-m4a".equals(contentType)) {
            return file;
        }

        try {
            byte[] bytes = file.getBytes();

            String originalFilename = file.getOriginalFilename();
            String mp4Filename = changeExtensionToMp4(originalFilename);

            return new InMemoryMultipartFile(
                    file.getName(),           // form field name
                    mp4Filename,              // new filename
                    "audio/mp4",              // new content type
                    bytes
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read audio file bytes", e);
        }
    }

    private static String changeExtensionToMp4(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file.mp4";
        }
        int dot = filename.lastIndexOf('.');
        if (dot == -1) {
            return filename + ".mp4";
        }
        return filename.substring(0, dot) + ".mp4";
    }

    private static class InMemoryMultipartFile implements MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] bytes;

        private InMemoryMultipartFile(String name,
                                      String originalFilename,
                                      String contentType,
                                      byte[] bytes) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.bytes = bytes != null ? bytes : new byte[0];
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return bytes.length == 0;
        }

        @Override
        public long getSize() {
            return bytes.length;
        }

        @Override
        public byte[] getBytes() {
            return bytes;
        }

        @Override
        public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(bytes);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException {
            java.nio.file.Files.write(dest.toPath(), bytes);
        }
    }
}
