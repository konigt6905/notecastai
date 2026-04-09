package com.notecastai.integration.ai.provider.openai.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OpenAiSpeechModel {

    GPT4O_MINI_TTS("gpt-4o-mini-tts", 1.0),
    GPT4O_AUDIO_PREVIEW("gpt-4o-audio-preview", 1.0);

    private final String modelId;
    private final double defaultSpeed;
}
