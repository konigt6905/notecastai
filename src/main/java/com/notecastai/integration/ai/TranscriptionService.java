package com.notecastai.integration.ai;

import com.notecastai.integration.ai.dto.TranscriptionResult;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public interface TranscriptionService {

    CompletableFuture<TranscriptionResult> transcribeAudioFile(
            InputStream audioStream,
            String filename,
            String contentType,
            TranscriptionLanguage language
    );

}
