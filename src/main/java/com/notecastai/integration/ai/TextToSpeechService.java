package com.notecastai.integration.ai;

import com.notecastai.integration.ai.dto.TextToSpeechRequest;
import com.notecastai.integration.ai.dto.TextToSpeechResult;

public interface TextToSpeechService {

    TextToSpeechResult synthesizeSpeech(TextToSpeechRequest request);

}
