package com.notecastai.notecast.domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enumerates the actual OpenAI TTS voices available in the platform.
 * Each entry maps directly to a {@link TtsVoice} enum constant.
 */
public enum TtsVoicesOpenAi {

    ALLOY(TtsVoice.ALLOY),
    VERSE(TtsVoice.VERSE),
    ATTICUS(TtsVoice.ATTICUS),
    LYRIC(TtsVoice.LYRIC),
    ORION(TtsVoice.ORION),
    EMBER(TtsVoice.EMBER),
    SOL(TtsVoice.SOL),
    NOVA(TtsVoice.NOVA),
    LUMEN(TtsVoice.LUMEN),
    CALLIOPE(TtsVoice.CALLIOPE);

    private final TtsVoice voice;

    TtsVoicesOpenAi(TtsVoice voice) {
        this.voice = voice;
    }

    public TtsVoice getVoice() {
        return voice;
    }

    public static List<TtsVoice> list() {
        return Arrays.stream(values())
                .map(TtsVoicesOpenAi::getVoice)
                .collect(Collectors.toList());
    }

    public static TtsVoice defaultVoice() {
        return ALLOY.voice;
    }
}
