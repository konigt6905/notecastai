package com.notecastai.integration.ai.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TextToSpeechFormat {

    MP3("mp3", "audio/mpeg", ".mp3"),
    WAV("wav", "audio/wav", ".wav"),
    AAC("aac", "audio/aac", ".aac"),
    FLAC("flac", "audio/flac", ".flac"),
    PCM16("pcm", "audio/x-wav", ".wav"),
    OPUS("opus", "audio/opus", ".opus");

    private final String code;
    private final String contentType;
    private final String fileExtension;
}
