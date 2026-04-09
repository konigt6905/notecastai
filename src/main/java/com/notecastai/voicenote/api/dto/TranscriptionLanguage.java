package com.notecastai.voicenote.api.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TranscriptionLanguage {

    // Most common languages
    ENGLISH("en", "English"),
    SPANISH("es", "Spanish"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    ITALIAN("it", "Italian"),
    PORTUGUESE("pt", "Portuguese"),
    DUTCH("nl", "Dutch"),
    RUSSIAN("ru", "Russian"),
    CHINESE("zh", "Chinese"),
    JAPANESE("ja", "Japanese"),
    KOREAN("ko", "Korean"),
    ARABIC("ar", "Arabic"),
    HINDI("hi", "Hindi"),
    TURKISH("tr", "Turkish"),
    POLISH("pl", "Polish"),
    UKRAINIAN("uk", "Ukrainian"),
    CZECH("cs", "Czech"),
    SWEDISH("sv", "Swedish"),
    DANISH("da", "Danish"),
    NORWEGIAN("no", "Norwegian"),
    FINNISH("fi", "Finnish"),
    GREEK("el", "Greek"),
    HEBREW("he", "Hebrew"),
    INDONESIAN("id", "Indonesian"),
    MALAY("ms", "Malay"),
    THAI("th", "Thai"),
    VIETNAMESE("vi", "Vietnamese"),

    // Auto-detect
    AUTO("auto", "Auto-detect");

    /** ISO-639-1 language code */
    private final String code;

    /** Human-readable language name */
    private final String displayName;

    /**
     * Get language by code
     */
    public static TranscriptionLanguage fromCode(String code) {
        if (code == null || code.isBlank()) {
            return AUTO;
        }

        for (TranscriptionLanguage lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }

        return AUTO;
    }
}