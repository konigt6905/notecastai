package com.notecastai.notecast.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TtsVoice {

    // Female voices
    SARAH_EN("voice-sarah-en", "Sarah (English)", "en", "female",
            "Professional and clear American English voice",
            "voices/samples/sarah-en-sample.mp3"),

    EMMA_EN("voice-emma-en", "Emma (English)", "en", "female",
            "Warm and friendly British English voice",
            "voices/samples/emma-en-sample.mp3"),

    SOPHIA_EN("voice-sophia-en", "Sophia (English)", "en", "female",
            "Energetic and engaging voice with natural intonation",
            "voices/samples/sophia-en-sample.mp3"),

    // Male voices
    JAMES_EN("voice-james-en", "James (English)", "en", "male",
            "Deep and authoritative American English voice",
            "voices/samples/james-en-sample.mp3"),

    OLIVER_EN("voice-oliver-en", "Oliver (English)", "en", "male",
            "Friendly and conversational British English voice",
            "voices/samples/oliver-en-sample.mp3"),

    MICHAEL_EN("voice-michael-en", "Michael (English)", "en", "male",
            "Clear and professional narrator voice",
            "voices/samples/michael-en-sample.mp3"),

    // International voices
    MARIE_FR("voice-marie-fr", "Marie (French)", "fr", "female",
            "Native French speaker with Parisian accent",
            "voices/samples/marie-fr-sample.mp3"),

    HANS_DE("voice-hans-de", "Hans (German)", "de", "male",
            "Clear German voice with standard Hochdeutsch pronunciation",
            "voices/samples/hans-de-sample.mp3"),

    LUCIA_ES("voice-lucia-es", "Lucia (Spanish)", "es", "female",
            "Natural Castilian Spanish voice",
            "voices/samples/lucia-es-sample.mp3"),

    YUKI_JA("voice-yuki-ja", "Yuki (Japanese)", "ja", "female",
            "Native Japanese speaker with Tokyo accent",
            "voices/samples/yuki-ja-sample.mp3");

    private final String id;
    private final String name;
    private final String language;
    private final String gender;
    private final String description;
    private final String s3SamplePath;

    /**
     * Find a voice by its ID
     * @param id The voice ID to search for
     * @return The TtsVoice enum value, or null if not found
     */
    public static TtsVoice fromId(String id) {
        for (TtsVoice voice : values()) {
            if (voice.getId().equals(id)) {
                return voice;
            }
        }
        return null;
    }

    /**
     * Get the default voice
     * @return The default TTS voice (Sarah English)
     */
    public static TtsVoice getDefault() {
        return SARAH_EN;
    }
}