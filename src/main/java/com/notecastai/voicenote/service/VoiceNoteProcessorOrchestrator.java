package com.notecastai.voicenote.service;

import com.notecastai.integration.ai.provider.groq.dto.TranscriptionResult;
import com.notecastai.integration.storage.StorageService;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import com.notecastai.voicenote.domain.VoiceNoteStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceNoteProcessorOrchestrator {

    VoiceNoteService voiceNoteService;
    StorageService storageService;

    @Transactional
    public void processVoiceNoteAsync(Long voiceNoteId, MultipartFile file, TranscriptionLanguage preferredLanguage) {
        try {
            byte[] fileBytes = file.getBytes();
            String s3Key = buildS3Key(voiceNoteId, file.getOriginalFilename());

            voiceNoteService.updateStatus(voiceNoteId, VoiceNoteStatus.PROCESSING);

            // Start both async operations in parallel
            CompletableFuture<String> uploadFuture = storageService.putAsync(
                    s3Key,
                    new ByteArrayInputStream(fileBytes),
                    file.getSize(),
                    file.getContentType()
            );

            CompletableFuture<TranscriptionResult> transcriptionFuture =
                    voiceNoteService.transcribeAsync(
                            new ByteArrayInputStream(fileBytes),
                            file.getOriginalFilename(),
                            file.getContentType(),
                            preferredLanguage
                    );

            // Join both futures and update entity
            CompletableFuture.allOf(uploadFuture, transcriptionFuture)
                    .thenAccept(v -> {
                        try {
                            String s3FileUrl = uploadFuture.join();
                            TranscriptionResult transcription = transcriptionFuture.join();

                            voiceNoteService.saveTranscriptionResult(voiceNoteId, s3FileUrl, transcription);
                            log.info("Voice note processing completed successfully: {}", voiceNoteId);
                        } catch (Exception e) {
                            log.error("Error completing voice note processing: {}", voiceNoteId, e);
                            voiceNoteService.updateWithError(voiceNoteId, "Error completing processing: " + e.getMessage());
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("Error processing voice note: {}", voiceNoteId, ex);
                        voiceNoteService.updateWithError(voiceNoteId, "Processing failed: " + ex.getMessage());
                        return null;
                    });

        } catch (IOException e) {
            log.error("Failed to read file bytes for voice note: {}", voiceNoteId, e);

            voiceNoteService.updateWithError(voiceNoteId, "Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing voice note: {}", voiceNoteId, e);
            voiceNoteService.updateWithError(voiceNoteId, "Unexpected error: " + e.getMessage());
        }
    }

    private String buildS3Key(Long voiceNoteId, String filename) {
        return String.format("voice-notes/%d/%s", voiceNoteId, filename);
    }

}