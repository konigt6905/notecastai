package com.notecastai.integration.ai;

import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.domain.TranscriptSize;

public interface NoteCastTranscriptGenerator {

    /**
     * Generate TTS-friendly transcript from note content
     * @param noteContent The formatted note content
     * @param style The style to apply to the transcript
     * @return CompletableFuture containing the generated transcript
     */
    String generateTranscript(String noteContent, NoteCastStyle style, TranscriptSize size);
}