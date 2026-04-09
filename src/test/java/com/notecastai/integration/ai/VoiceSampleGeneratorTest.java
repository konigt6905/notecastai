package com.notecastai.integration.ai;

import com.notecastai.integration.ai.dto.TextToSpeechFormat;
import com.notecastai.integration.ai.dto.TextToSpeechRequest;
import com.notecastai.integration.ai.dto.TextToSpeechResult;
import com.notecastai.notecast.domain.TranscriptSize;
import com.notecastai.notecast.domain.TtsVoice;
import com.notecastai.notecast.domain.TtsVoiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Test class to generate sample audio files for all OpenAI TTS voices.
 * This creates WAV files in src/main/resources/audio/openai/ directory.
 *
 * Run this test manually when you want to regenerate voice samples.
 */
@SpringBootTest
@ActiveProfiles("test")
class VoiceSampleGeneratorTest {

    private static final Logger log = LoggerFactory.getLogger(VoiceSampleGeneratorTest.class);

    private static final String SAMPLE_TEXT =
            "I am your notes voice assistant. I can create a quick recap or a comprehensive deep dive in your knowledge base.";

    @Autowired
    private TextToSpeechService textToSpeechService;

    //@Test
    void generateAllOpenAiVoiceSamples() throws IOException {
        log.info("Starting OpenAI voice sample generation...");

        // Get all OpenAI voices
        List<TtsVoice> openAiVoices = TtsVoice.listByProvider(TtsVoiceProvider.OPENAI);

        log.info("Found {} OpenAI voices to generate samples for", openAiVoices.size());

        // Create output directory if it doesn't exist
        Path outputDir = Paths.get("src/main/resources/audio/openai");
        Files.createDirectories(outputDir);
        log.info("Output directory: {}", outputDir.toAbsolutePath());

        int successCount = 0;
        int failCount = 0;

        for (TtsVoice voice : openAiVoices) {
            try {
                log.info("Generating sample for voice: {} ({})", voice.getName(), voice.getId());

                // Create TTS request
                TextToSpeechRequest request = TextToSpeechRequest.builder()
                        .transcript(SAMPLE_TEXT)
                        .voice(voice)
                        .format(TextToSpeechFormat.WAV)
                        .size(TranscriptSize.MEDIUM)
                        .build();

                // Generate speech
                TextToSpeechResult result = textToSpeechService.synthesizeSpeech(request);

                // Save to file
                String filename = voice.getId() + ".wav";
                Path outputFile = outputDir.resolve(filename);

                Files.write(
                    outputFile,
                    result.getAudioBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                );

                log.info("✓ Successfully generated: {} ({} bytes, ~{}s)",
                    filename,
                    result.getSizeBytes(),
                    result.getEstimatedDurationSeconds());

                successCount++;

            } catch (Exception e) {
                log.error("✗ Failed to generate sample for voice: {}", voice.getName(), e);
                failCount++;
            }
        }

        log.info("Voice sample generation complete!");
        log.info("Success: {}, Failed: {}, Total: {}", successCount, failCount, openAiVoices.size());

        if (failCount > 0) {
            throw new RuntimeException("Some voice samples failed to generate. Check logs for details.");
        }
    }

    //@Test
    void generateSingleVoiceSample() throws IOException {
        // Example: Generate just one voice for quick testing
        TtsVoice voice = TtsVoice.ALLOY;

        log.info("Generating single sample for voice: {}", voice.getName());

        TextToSpeechRequest request = TextToSpeechRequest.builder()
                .transcript(SAMPLE_TEXT)
                .voice(voice)
                .format(TextToSpeechFormat.WAV)
                .size(TranscriptSize.MEDIUM)
                .build();

        TextToSpeechResult result = textToSpeechService.synthesizeSpeech(request);

        Path outputDir = Paths.get("src/main/resources/audio/openai");
        Files.createDirectories(outputDir);

        Path outputFile = outputDir.resolve(voice.getId() + ".wav");
        Files.write(outputFile, result.getAudioBytes());

        log.info("Sample generated: {} ({} bytes)", outputFile.toAbsolutePath(), result.getSizeBytes());
    }
}
