package com.notecastai.notecast.domain;

import com.notecastai.common.exeption.BusinessException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum TtsVoice {

    // Female voices - Kokoro TTS
    BELLA("bella_af", "Bella", "en", "female",
            "Warm and pleasant female voice with natural intonation",
            "audio/kokoro/bella.wav",
            TtsVoiceProvider.KOKORO),

    NICOLE("nicole_af", "Nicole", "en", "female",
            "Friendly and approachable female voice, clear and expressive",
            "audio/kokoro/nicole.wav",
            TtsVoiceProvider.KOKORO),

    SKY("sky_af", "Sky", "en", "female",
            "Light and airy female voice with ethereal quality",
            "audio/kokoro/sky.wav",
            TtsVoiceProvider.KOKORO),

    ECHO_K("echo_af", "Echo", "en", "female",
            "Ethereal female voice with unique resonant characteristics",
            "audio/kokoro/echo.wav",
            TtsVoiceProvider.KOKORO),

    // Male voices - Kokoro TTS
    LIAM("liam_am", "Liam", "en", "male",
            "Smooth and engaging male voice with natural cadence",
            "audio/kokoro/liam.wav",
            TtsVoiceProvider.KOKORO),

    MICHAEL("am_michael", "Michael", "en", "male",
            "Professional and clear male voice, excellent for narration",
            "audio/kokoro/michael.wav",
            TtsVoiceProvider.KOKORO),

    ERIC("eric_am", "Eric", "en", "male",
            "Professional and articulate male voice with neutral accent",
            "audio/kokoro/eric.wav",
            TtsVoiceProvider.KOKORO),

    HART("hart_am", "Hart", "en", "male",
            "Strong and bold male voice with commanding presence",
            "audio/kokoro/hart.wav",
            TtsVoiceProvider.KOKORO),

    PUNCH("punch_am", "Punch", "en", "male",
            "Energetic and punchy male voice with dynamic delivery",
            "audio/kokoro/punch.wav",
            TtsVoiceProvider.KOKORO),

    FENRIR("fenrir_am", "Fenrir", "en", "male",
            "Deep and powerful male voice with authoritative tone",
            "audio/kokoro/renrir.wav",
            TtsVoiceProvider.KOKORO),

    // OpenAI voices - Updated to match current API
    ALLOY("alloy", "Alloy", "en", "neutral",
            "Balanced OpenAI voice with warm tone suitable for narration and explainers.",
            "audio/openai/alloy.wav",
            TtsVoiceProvider.OPENAI),

    ECHO("echo", "Echo", "en", "male",
            "Deep resonant OpenAI male voice with commanding presence.",
            "audio/openai/echo.wav",
            TtsVoiceProvider.OPENAI),

    FABLE("fable", "Fable", "en", "neutral",
            "Storytelling OpenAI voice ideal for narration and audiobooks.",
            "audio/openai/fable.wav",
            TtsVoiceProvider.OPENAI),

    ONYX("onyx", "Onyx", "en", "male",
            "Deep authoritative OpenAI male voice perfect for professional content.",
            "audio/openai/onyx.wav",
            TtsVoiceProvider.OPENAI),

    NOVA("nova", "Nova", "en", "female",
            "Energetic OpenAI voice perfect for marketing and social media clips.",
            "audio/openai/nova.wav",
            TtsVoiceProvider.OPENAI),

    SHIMMER("shimmer", "Shimmer", "en", "female",
            "Bright and uplifting OpenAI female voice with positive energy.",
            "audio/openai/shimmer.wav",
            TtsVoiceProvider.OPENAI),

    CORAL("coral", "Coral", "en", "female",
            "Warm and friendly OpenAI female voice for conversational content.",
            "audio/openai/coral.wav",
            TtsVoiceProvider.OPENAI),

    VERSE("verse", "Verse", "en", "female",
            "Expressive OpenAI female voice with gentle pacing for conversational content.",
            "audio/openai/verse.wav",
            TtsVoiceProvider.OPENAI),

    BALLAD("ballad", "Ballad", "en", "female",
            "Melodic OpenAI female voice with lyrical quality.",
            "audio/openai/ballad.wav",
            TtsVoiceProvider.OPENAI),

    ASH("ash", "Ash", "en", "male",
            "Calm and steady OpenAI male voice for professional narration.",
            "audio/openai/ash.wav",
            TtsVoiceProvider.OPENAI),

    SAGE("sage", "Sage", "en", "neutral",
            "Wise and mature OpenAI voice for educational and informative content.",
            "audio/openai/sage.wav",
            TtsVoiceProvider.OPENAI),

    MARIN("marin", "Marin", "en", "female",
            "Crisp and clear OpenAI female voice for articulate delivery.",
            "audio/openai/marin.wav",
            TtsVoiceProvider.OPENAI),

    CEDAR("cedar", "Cedar", "en", "male",
            "Rich and warm OpenAI male voice with natural tone.",
            "audio/openai/cedar.wav",
            TtsVoiceProvider.OPENAI);

    private final String id;
    private final String name;
    private final String language;
    private final String gender;
    private final String description;
    private final String sampleResourcePath;
    private final TtsVoiceProvider provider;

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

    public static TtsVoice getDefault() {
        return BELLA;
    }

    public static TtsVoice getDefault(TtsVoiceProvider provider) {
        return listByProvider(provider).stream()
                .findFirst()
                .orElse(getDefault());
    }

    public boolean supportsProvider(TtsVoiceProvider desiredProvider) {
        return this.provider == desiredProvider;
    }

    public static List<TtsVoice> listByProvider(TtsVoiceProvider provider) {
        return Arrays.stream(values())
                .filter(voice -> voice.provider == provider)
                .collect(Collectors.toList());
    }
}
