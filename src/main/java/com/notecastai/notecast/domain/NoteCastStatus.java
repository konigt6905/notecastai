package com.notecastai.notecast.domain;

public enum NoteCastStatus {
    WAITING_FOR_TRANSCRIPT,
    PROCESSING_TRANSCRIPT,
    WAITING_FOR_TTS,
    PROCESSING_TTS,
    PROCESSED,
    FAILED
}
