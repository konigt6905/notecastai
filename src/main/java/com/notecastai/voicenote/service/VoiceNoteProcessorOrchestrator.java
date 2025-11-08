package com.notecastai.voicenote.service;

import com.notecastai.common.exeption.BusinessException;
import com.notecastai.integration.ai.provider.groq.dto.TranscriptionResult;
import com.notecastai.integration.storage.StorageService;
import com.notecastai.note.api.dto.CreateNoteRequest;
import com.notecastai.note.api.dto.NoteDTO;
import com.notecastai.note.domain.NoteType;
import com.notecastai.note.service.NoteService;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import com.notecastai.voicenote.api.dto.VoiceNoteCreateRequest;
import com.notecastai.voicenote.api.dto.VoiceNoteDTO;
import com.notecastai.voicenote.domain.VoiceNoteStatus;
import com.notecastai.voicenote.service.impl.VoiceNoteHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.concurrent.CompletableFuture;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.INTERNAL_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceNoteProcessorOrchestrator {

    private final StorageService storageService;
    private final VoiceNoteHelper voiceNoteHelper;
    private final NoteService noteService;

    public VoiceNoteDTO processVoiceNote(Long voiceNoteId, MultipartFile file, TranscriptionLanguage preferredLanguage, VoiceNoteCreateRequest request) {
        try {
            byte[] bytes = file.getBytes();
            String s3Key = buildS3Key(voiceNoteId, file.getOriginalFilename());

            voiceNoteHelper.updateStatus(voiceNoteId, VoiceNoteStatus.PROCESSING);

            CompletableFuture<String> uploadFut = storageService.putAsync(
                    s3Key, new ByteArrayInputStream(bytes), file.getSize(), file.getContentType()
            );

            CompletableFuture<TranscriptionResult> transFut = voiceNoteHelper.transcribeAsync(
                    new ByteArrayInputStream(bytes), file.getOriginalFilename(), file.getContentType(), preferredLanguage
            );

            // Wait and combine results
            String s3Url = uploadFut.join();
            TranscriptionResult tr = transFut.join();

            NoteDTO note = noteService.create(CreateNoteRequest.builder()
                    .title(request.getTitle())
                    .tagIds(request.getTagIds())
                    .type(NoteType.VOICENOTE)
                    .knowledgeBase(tr.getTranscript())
                    .formateType(request.getFormateType())
                    .instructions(request.getUserInstructions())
                    .build());

            VoiceNoteDTO voiceNote = voiceNoteHelper.saveTranscriptionResult(voiceNoteId, s3Url, tr);

            log.info("Voice note processing completed successfully: {}", voiceNoteId);
            return voiceNote.withNote(note);
        } catch (Exception e) {
            log.error("Processing failed for voiceNoteId={}", voiceNoteId, e);
            voiceNoteHelper.updateWithError(voiceNoteId, "Processing failed: " + e.getMessage());
            throw BusinessException.of(INTERNAL_ERROR.append(" Error processing voice note"));
        }
    }

    private String buildS3Key(Long voiceNoteId, String filename) {
        return String.format("voice-notes/%d/%s", voiceNoteId, filename);
    }

}