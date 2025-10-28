package com.notecastai.integration.ai.provider.groq.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class GroqClientConfig {

    @Bean
    RestClient groqRestClient(
            @Value("${ai.groq.api.url:https://api.groq.com/openai/v1}") String apiUrl,
            @Value("${ai.groq.api.key}") String apiKey
    ) {
        var requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(120)); // Transcription can take time

        return RestClient.builder()
                .baseUrl(apiUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }
}
