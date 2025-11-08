package com.notecastai.notecast.domain;

import com.notecastai.common.exeption.BusinessException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TtsVoice {

    // Female voices - Kokoro TTS
    BELLA("bella_af", "Bella", "en", "female",
            "Warm and pleasant female voice with natural intonation",
            "audio/bella.wav"),

    NICOLE("nicole_af", "Nicole", "en", "female",
            "Friendly and approachable female voice, clear and expressive",
            "audio/nicole.wav"),

    SKY("sky_af", "Sky", "en", "female",
            "Light and airy female voice with ethereal quality",
            "audio/sky.wav"),

    ECHO("echo_af", "Echo", "en", "female",
            "Ethereal female voice with unique resonant characteristics",
            "audio/echo.wav"),

    // Male voices - Kokoro TTS
    LIAM("liam_am", "Liam", "en", "male",
            "Smooth and engaging male voice with natural cadence",
            "audio/liam.wav"),

    MICHAEL("am_michael", "Michael", "en", "male",
            "Professional and clear male voice, excellent for narration",
            "audio/michael.wav"),

    ERIC("eric_am", "Eric", "en", "male",
            "Professional and articulate male voice with neutral accent",
            "audio/eric.wav"),

    HART("hart_am", "Hart", "en", "male",
            "Strong and bold male voice with commanding presence",
            "audio/hart.wav"),

    PUNCH("punch_am", "Punch", "en", "male",
            "Energetic and punchy male voice with dynamic delivery",
            "audio/punch.wav"),

    FENRIR("fenrir_am", "Fenrir", "en", "male",
            "Deep and powerful male voice with authoritative tone",
            "audio/renrir.wav");

    private final String id;
    private final String name;
    private final String language;
    private final String gender;
    private final String description;
    private final String sampleResourcePath;

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
         throw BusinessException.of(BusinessException.BusinessCode.ENTITY_NOT_FOUND);
    }

    /**
     * Get the default voice
     * @return The default TTS voice (Bella)
     */
    public static TtsVoice getDefault() {
        return BELLA;
    }
}