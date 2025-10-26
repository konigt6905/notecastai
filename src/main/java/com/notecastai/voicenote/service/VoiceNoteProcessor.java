package com.notecastai.voicenote.service;

import com.notecastai.integration.ai.TranscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceNoteProcessor {

    private final TranscriptionService transcriptionService;

    @Async("voiceNoteProcessingExecutor")
    public CompletableFuture<TranscriptionService.TranscriptionResult> transcribeAsync(
            InputStream audioStream,
            String filename,
            String contentType
    ) {
        log.info("Starting async transcription for file: {}", filename);
        return transcriptionService.transcribe(audioStream, filename, contentType)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Transcription failed for file: {}", filename, error);
                    } else {
                        log.info("Transcription completed for file: {}", filename);
                    }
                });
    }
}