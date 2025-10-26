package com.notecastai.integration.ai;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public interface TranscriptionService {

    CompletableFuture<TranscriptionResult> transcribe(InputStream audioStream, String filename, String contentType);

    record TranscriptionResult(String transcript, String language, Integer durationSeconds) {}
}
