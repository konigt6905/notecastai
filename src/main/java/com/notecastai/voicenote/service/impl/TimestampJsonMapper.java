package com.notecastai.voicenote.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notecastai.integration.ai.dto.SegmentTimestamp;
import com.notecastai.integration.ai.dto.WordTimestamp;
import com.notecastai.voicenote.domain.TimestampJsonModels.SegmentTimestampJson;
import com.notecastai.voicenote.domain.TimestampJsonModels.SegmentTimestampsWrapper;
import com.notecastai.voicenote.domain.TimestampJsonModels.WordTimestampJson;
import com.notecastai.voicenote.domain.TimestampJsonModels.WordTimestampsWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimestampJsonMapper {

    private final ObjectMapper objectMapper;

    /**
     * Serialize word timestamps to JSON string
     */
    public String serializeWordTimestamps(List<WordTimestamp> wordTimestamps) {
        if (wordTimestamps == null || wordTimestamps.isEmpty()) {
            return null;
        }

        try {
            AtomicInteger sequence = new AtomicInteger(0);

            List<WordTimestampJson> jsonList = wordTimestamps.stream()
                    .map(wt -> WordTimestampJson.builder()
                            .word(wt.getWord())
                            .startTime(wt.getStartTime())
                            .endTime(wt.getEndTime())
                            .sequence(sequence.getAndIncrement())
                            .build())
                    .collect(Collectors.toList());

            WordTimestampsWrapper wrapper = WordTimestampsWrapper.builder()
                    .words(jsonList)
                    .build();

            return objectMapper.writeValueAsString(wrapper);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize word timestamps", e);
            return null;
        }
    }

    /**
     * Deserialize word timestamps from JSON string
     */
    public List<WordTimestamp> deserializeWordTimestamps(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        try {
            WordTimestampsWrapper wrapper = objectMapper.readValue(json, WordTimestampsWrapper.class);

            if (wrapper.getWords() == null) {
                return new ArrayList<>();
            }

            return wrapper.getWords().stream()
                    .map(wt -> WordTimestamp.builder()
                            .word(wt.getWord())
                            .startTime(wt.getStartTime())
                            .endTime(wt.getEndTime())
                            .build())
                    .collect(Collectors.toList());

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize word timestamps", e);
            return new ArrayList<>();
        }
    }

    /**
     * Serialize segment timestamps to JSON string
     */
    public String serializeSegmentTimestamps(List<SegmentTimestamp> segmentTimestamps) {
        if (segmentTimestamps == null || segmentTimestamps.isEmpty()) {
            return null;
        }

        try {
            List<SegmentTimestampJson> jsonList = segmentTimestamps.stream()
                    .map(st -> SegmentTimestampJson.builder()
                            .id(st.getId())
                            .text(st.getText())
                            .startTime(st.getStartTime())
                            .endTime(st.getEndTime())
                            .avgLogProb(st.getAverageLogProbability())
                            .compressionRatio(st.getCompressionRatio())
                            .noSpeechProb(st.getNoSpeechProbability())
                            .build())
                    .collect(Collectors.toList());

            SegmentTimestampsWrapper wrapper = SegmentTimestampsWrapper.builder()
                    .segments(jsonList)
                    .build();

            return objectMapper.writeValueAsString(wrapper);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize segment timestamps", e);
            return null;
        }
    }

    /**
     * Deserialize segment timestamps from JSON string
     */
    public List<SegmentTimestamp> deserializeSegmentTimestamps(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        try {
            SegmentTimestampsWrapper wrapper = objectMapper.readValue(json, SegmentTimestampsWrapper.class);

            if (wrapper.getSegments() == null) {
                return new ArrayList<>();
            }

            return wrapper.getSegments().stream()
                    .map(st -> SegmentTimestamp.builder()
                            .id(st.getId())
                            .text(st.getText())
                            .startTime(st.getStartTime())
                            .endTime(st.getEndTime())
                            .averageLogProbability(st.getAvgLogProb())
                            .compressionRatio(st.getCompressionRatio())
                            .noSpeechProbability(st.getNoSpeechProb())
                            .build())
                    .collect(Collectors.toList());

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize segment timestamps", e);
            return new ArrayList<>();
        }
    }
}
