package com.notecastai.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class Resilience4jConfig {

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                .retryOnException(e -> {
                    // Retry on validation exceptions and technical exceptions
                    log.warn("Retry triggered for exception: {}", e.getClass().getSimpleName());
                    return true;
                })
                .build();

        return RetryRegistry.of(config);
    }

    @Bean
    public Retry noteAiRetry(RetryRegistry retryRegistry) {
        Retry retry = retryRegistry.retry("noteAiRetry");

        retry.getEventPublisher()
                .onRetry(event -> log.warn("AI call retry attempt {} due to: {}",
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable().getMessage()))
                .onSuccess(event -> log.info("AI call succeeded after {} attempts",
                        event.getNumberOfRetryAttempts()))
                .onError(event -> log.error("AI call failed after {} attempts",
                        event.getNumberOfRetryAttempts()));

        return retry;
    }
}