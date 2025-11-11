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
    ECHO(TtsVoice.ECHO),
    FABLE(TtsVoice.FABLE),
    ONYX(TtsVoice.ONYX),
    NOVA(TtsVoice.NOVA),
    SHIMMER(TtsVoice.SHIMMER),
    CORAL(TtsVoice.CORAL),
    VERSE(TtsVoice.VERSE),
    BALLAD(TtsVoice.BALLAD),
    ASH(TtsVoice.ASH),
    SAGE(TtsVoice.SAGE),
    MARIN(TtsVoice.MARIN),
    CEDAR(TtsVoice.CEDAR);

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
