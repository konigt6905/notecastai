package com.notecastai.config;

import com.notecastai.notecast.domain.TtsVoiceProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.tts")
public class TtsVoiceProperties {

    private TtsVoiceProvider voiceProvider = TtsVoiceProvider.OPENAI;
}
