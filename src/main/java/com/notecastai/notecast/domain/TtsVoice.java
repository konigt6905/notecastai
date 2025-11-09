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

    ECHO("echo_af", "Echo", "en", "female",
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

    // OpenAI voices
    ALLOY("alloy", "Alloy", "en", "neutral",
            "Balanced OpenAI voice with warm tone suitable for narration and explainers.",
            "audio/openai/alloy.wav",
            TtsVoiceProvider.OPENAI),

    VERSE("verse", "Verse", "en", "female",
            "Expressive OpenAI female voice with gentle pacing for conversational content.",
            "audio/openai/verse.wav",
            TtsVoiceProvider.OPENAI),

    ATTICUS("atticus", "Atticus", "en", "male",
            "Deep cinematic OpenAI male voice ideal for dramatic storytelling.",
            "audio/openai/atticus.wav",
            TtsVoiceProvider.OPENAI),

    LYRIC("lyric", "Lyric", "en", "female",
            "Friendly OpenAI voice with bright tone for upbeat explainers.",
            "audio/openai/lyric.wav",
            TtsVoiceProvider.OPENAI),

    ORION("orion", "Orion", "en", "male",
            "Confident OpenAI voice with crisp delivery for professional narration.",
            "audio/openai/orion.wav",
            TtsVoiceProvider.OPENAI),

    EMBER("ember", "Ember", "en", "female",
            "Warm storyteller OpenAI voice with subtle emotional range.",
            "audio/openai/ember.wav",
            TtsVoiceProvider.OPENAI),

    SOL("sol", "Sol", "en", "male",
            "Relaxed OpenAI voice with smooth cadence for long-form listening.",
            "audio/openai/sol.wav",
            TtsVoiceProvider.OPENAI),

    NOVA("nova", "Nova", "en", "female",
            "Energetic OpenAI voice perfect for marketing and social media clips.",
            "audio/openai/nova.wav",
            TtsVoiceProvider.OPENAI),

    LUMEN("lumen", "Lumen", "en", "neutral",
            "Crystal-clear OpenAI neutral voice optimized for product explainers.",
            "audio/openai/lumen.wav",
            TtsVoiceProvider.OPENAI),

    CALLIOPE("calliope", "Calliope", "en", "female",
            "Theatrical OpenAI voice delivering expressive emphasis and flair.",
            "audio/openai/calliope.wav",
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
