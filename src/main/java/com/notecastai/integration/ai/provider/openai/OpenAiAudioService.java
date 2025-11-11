package com.notecastai.integration.ai.provider.openai;

import com.notecastai.integration.ai.TextToSpeechService;
import com.notecastai.integration.ai.TranscriptionService;
import com.notecastai.integration.ai.dto.SegmentTimestamp;
import com.notecastai.integration.ai.dto.TranscriptionMetadata;
import com.notecastai.integration.ai.dto.TranscriptionResult;
import com.notecastai.integration.ai.dto.WordTimestamp;
import com.notecastai.integration.ai.provider.openai.client.OpenAiClient;
import com.notecastai.integration.ai.provider.openai.dto.OpenAiSpeechModel;
import com.notecastai.integration.ai.provider.openai.dto.OpenAiSpeechRequest;
import com.notecastai.integration.ai.provider.openai.dto.OpenAiSpeechResponse;
import com.notecastai.integration.ai.provider.openai.dto.OpenAiTranscriptionModel;
import com.notecastai.integration.ai.provider.openai.dto.OpenAiTranscriptionRequest;
import com.notecastai.integration.ai.provider.openai.dto.OpenAiTranscriptionResponse;
import com.notecastai.integration.ai.dto.TextToSpeechFormat;
import com.notecastai.integration.ai.dto.TextToSpeechRequest;
import com.notecastai.integration.ai.dto.TextToSpeechResult;
import com.notecastai.notecast.domain.TranscriptSize;
import com.notecastai.notecast.domain.TtsVoice;
import com.notecastai.notecast.domain.TtsVoiceProvider;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service("openAiAudioService")
@RequiredArgsConstructor
public class OpenAiAudioService implements TranscriptionService, TextToSpeechService {

    private static final OpenAiTranscriptionModel DEFAULT_TRANSCRIPTION_MODEL = OpenAiTranscriptionModel.WHISPER_1;
    private static final OpenAiSpeechModel DEFAULT_SPEECH_MODEL = OpenAiSpeechModel.GPT4O_MINI_TTS;
    private static final double WORDS_PER_SECOND = 2.5; // ~150 wpm

    private static final Map<TtsVoice, String> VOICE_MAPPING = new EnumMap<>(TtsVoice.class);

    static {
        // Map Kokoro voices to OpenAI voices
        VOICE_MAPPING.put(TtsVoice.BELLA, "alloy");
        VOICE_MAPPING.put(TtsVoice.NICOLE, "verse");
        VOICE_MAPPING.put(TtsVoice.SKY, "shimmer");
        VOICE_MAPPING.put(TtsVoice.LIAM, "onyx");
        VOICE_MAPPING.put(TtsVoice.MICHAEL, "ash");
        VOICE_MAPPING.put(TtsVoice.ERIC, "cedar");
        VOICE_MAPPING.put(TtsVoice.HART, "onyx");
        VOICE_MAPPING.put(TtsVoice.PUNCH, "echo");
        VOICE_MAPPING.put(TtsVoice.FENRIR, "echo");
    }

    private final OpenAiClient openAiClient;
    private final Retry noteAiRetry;

    @Override
    public CompletableFuture<TranscriptionResult> transcribeAudioFile(
            InputStream audioStream,
            String filename,
            String contentType,
            TranscriptionLanguage language
    ) {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();

            OpenAiTranscriptionRequest request = OpenAiTranscriptionRequest.builder()
                    .audioStream(audioStream)
                    .filename(filename)
                    .contentType(contentType)
                    .language(language != null ? language : TranscriptionLanguage.AUTO)
                    .model(DEFAULT_TRANSCRIPTION_MODEL)
                    .build();

            OpenAiTranscriptionResponse response = Retry.decorateSupplier(
                    noteAiRetry,
                    () -> openAiClient.transcribe(request)
            ).get();

            long processingTime = System.currentTimeMillis() - start;

            log.info("OpenAI transcription completed: lang={}, duration={}s, model={}",
                    response.getLanguage(),
                    response.getDuration(),
                    DEFAULT_TRANSCRIPTION_MODEL.getModelId());

            return mapToTranscriptionResult(response, processingTime);
        });
    }

    @Override
    public TextToSpeechResult synthesizeSpeech(TextToSpeechRequest request) {
        validateRequest(request);

        TextToSpeechFormat format = request.getFormat() != null ? request.getFormat() : TextToSpeechFormat.MP3;
        String voice = resolveVoice(request.getVoice());
        double speed = resolveSpeed(request.getSize());

        OpenAiSpeechRequest speechRequest = OpenAiSpeechRequest.builder()
                .model(DEFAULT_SPEECH_MODEL.getModelId())
                .input(request.getTranscript())
                .voice(voice)
                .responseFormat(format.getCode())
                .speed(speed)
                .build();

        MediaType acceptType = MediaType.parseMediaType(format.getContentType());
        long start = System.currentTimeMillis();

        OpenAiSpeechResponse response = Retry.decorateSupplier(
                noteAiRetry,
                () -> openAiClient.createSpeech(speechRequest, acceptType)
        ).get();

        long processingTime = System.currentTimeMillis() - start;
        byte[] audioBytes = response.getAudio();

        double estimatedDuration = estimateDurationSeconds(request.getTranscript());

        log.info("OpenAI TTS completed for refId={}, voice={}, format={}, size={} bytes",
                request.getReferenceId(),
                voice,
                format,
                audioBytes != null ? audioBytes.length : 0);

        return TextToSpeechResult.builder()
                .audioBytes(audioBytes)
                .mediaType(response.getMediaType() != null
                        ? response.getMediaType().toString()
                        : format.getContentType())
                .fileExtension(format.getFileExtension())
                .provider("OpenAI")
                .model(DEFAULT_SPEECH_MODEL.getModelId())
                .voice(voice)
                .processingTimeMs(processingTime)
                .sizeBytes(audioBytes != null ? (long) audioBytes.length : 0L)
                .estimatedDurationSeconds(estimatedDuration)
                .requestId(response.getRequestId())
                .build();
    }

    private void validateRequest(TextToSpeechRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("TextToSpeechRequest cannot be null");
        }
        if (request.getTranscript() == null || request.getTranscript().isBlank()) {
            throw new IllegalArgumentException("Transcript cannot be empty");
        }
    }

    private double estimateDurationSeconds(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            return 0;
        }
        int wordCount = transcript.trim().split("\\s+").length;
        return Math.round((wordCount / WORDS_PER_SECOND) * 10.0) / 10.0;
    }

    private String resolveVoice(TtsVoice voice) {
        if (voice == null) {
            return "alloy";
        }

        if (voice.supportsProvider(TtsVoiceProvider.OPENAI)) {
            return voice.getId();
        }

        return VOICE_MAPPING.getOrDefault(voice, "alloy");
    }

    private double resolveSpeed(TranscriptSize size) {
        if (size == null) {
            return DEFAULT_SPEECH_MODEL.getDefaultSpeed();
        }

        return switch (size) {
            case EXTRA_SHORT, SHORT -> 1.05;
            case MEDIUM -> 1.0;
            case LARGE -> 0.97;
            case EXTRA_LARGE -> 0.94;
        };
    }

    private TranscriptionResult mapToTranscriptionResult(OpenAiTranscriptionResponse response, long processingTimeMs) {
        List<WordTimestamp> wordTimestamps = null;
        if (response.getWords() != null) {
            wordTimestamps = response.getWords().stream()
                    .map(word -> WordTimestamp.builder()
                            .word(word.getWord())
                            .startTime(word.getStart())
                            .endTime(word.getEnd())
                            .build())
                    .collect(Collectors.toList());
        }

        List<SegmentTimestamp> segmentTimestamps = null;
        if (response.getSegments() != null) {
            segmentTimestamps = response.getSegments().stream()
                    .map(segment -> SegmentTimestamp.builder()
                            .id(segment.getId())
                            .text(segment.getText())
                            .startTime(segment.getStart())
                            .endTime(segment.getEnd())
                            .averageLogProbability(segment.getAvgLogprob())
                            .compressionRatio(segment.getCompressionRatio())
                            .noSpeechProbability(segment.getNoSpeechProb())
                            .build())
                    .collect(Collectors.toList());
        }

        Integer durationSeconds = null;
        if (response.getDuration() != null) {
            durationSeconds = (int) Math.round(response.getDuration());
        } else if (segmentTimestamps != null && !segmentTimestamps.isEmpty()) {
            SegmentTimestamp last = segmentTimestamps.get(segmentTimestamps.size() - 1);
            if (last.getEndTime() != null) {
                durationSeconds = last.getEndTime().intValue();
            }
        }

        TranscriptionMetadata metadata = TranscriptionMetadata.builder()
                .modelUsed(DEFAULT_TRANSCRIPTION_MODEL.getModelId())
                .provider("OpenAI")
                .requestId(response.getRequestId())
                .processingTimeMs(processingTimeMs)
                .build();

        return TranscriptionResult.builder()
                .transcript(response.getText())
                .language(response.getLanguage())
                .durationSeconds(durationSeconds)
                .wordTimestamps(wordTimestamps)
                .segmentTimestamps(segmentTimestamps)
                .metadata(metadata)
                .build();
    }
}
