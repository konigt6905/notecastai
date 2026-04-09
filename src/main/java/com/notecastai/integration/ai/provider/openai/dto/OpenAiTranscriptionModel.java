package com.notecastai.integration.ai.provider.openai.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OpenAiTranscriptionModel {

    WHISPER_1("whisper-1", true),
    GPT4O_MINI_TRANSCRIBE("gpt-4o-mini-transcribe", true);

    private final String modelId;
    private final boolean supportsVerboseJson;
}
