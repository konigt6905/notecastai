package com.notecastai.integration.ai.provider.groq.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Groq Whisper models for audio transcription
 */
@Getter
@RequiredArgsConstructor
public enum GroqWhisperModel {

    WHISPER_LARGE_V3_TURBO("whisper-large-v3-turbo"),

    WHISPER_LARGE_V3("whisper-large-v3");

    private final String modelId;

}