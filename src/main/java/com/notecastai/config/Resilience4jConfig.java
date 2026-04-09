package com.notecastai.config;

import com.notecastai.common.exception.AiValidationException;
import com.notecastai.common.exception.TechnicalException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class Resilience4jConfig {

    @Value("${rate-limit.global-ai.limit-per-period:60}")
    private int globalAiLimitPerPeriod;

    @Value("${rate-limit.global-ai.period-seconds:60}")
    private int globalAiPeriodSeconds;

    @Value("${rate-limit.global-tts.limit-per-period:20}")
    private int globalTtsLimitPerPeriod;

    @Value("${rate-limit.global-tts.period-seconds:60}")
    private int globalTtsPeriodSeconds;

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig aiConfig = RateLimiterConfig.custom()
                .limitForPeriod(globalAiLimitPerPeriod)
                .limitRefreshPeriod(Duration.ofSeconds(globalAiPeriodSeconds))
                .timeoutDuration(Duration.ZERO) // fail immediately
                .build();

        RateLimiterConfig ttsConfig = RateLimiterConfig.custom()
                .limitForPeriod(globalTtsLimitPerPeriod)
                .limitRefreshPeriod(Duration.ofSeconds(globalTtsPeriodSeconds))
                .timeoutDuration(Duration.ZERO)
                .build();

        return RateLimiterRegistry.of(
                java.util.Map.of("globalAi", aiConfig, "globalTts", ttsConfig)
        );
    }

    @Bean
    public RateLimiter globalAiRateLimiter(RateLimiterRegistry registry) {
        RateLimiter limiter = registry.rateLimiter("globalAi");

        limiter.getEventPublisher()
                .onSuccess(event -> log.debug("globalAi rate limiter permitted call"))
                .onFailure(event -> log.warn("globalAi rate limiter rejected call, limit exhausted"));

        return limiter;
    }

    @Bean
    public RateLimiter globalTtsRateLimiter(RateLimiterRegistry registry) {
        RateLimiter limiter = registry.rateLimiter("globalTts");

        limiter.getEventPublisher()
                .onSuccess(event -> log.debug("globalTts rate limiter permitted call"))
                .onFailure(event -> log.warn("globalTts rate limiter rejected call, limit exhausted"));

        return limiter;
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                .retryExceptions(TechnicalException.class, AiValidationException.class)
                .ignoreExceptions(IllegalArgumentException.class)
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

    @Bean
    public Retry voiceNoteProcessingRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(3))
                .retryExceptions(TechnicalException.class, AiValidationException.class, RuntimeException.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        Retry retry = Retry.of("voiceNoteProcessingRetry", config);

        retry.getEventPublisher()
                .onRetry(event -> log.warn("Voice note processing retry attempt {} due to: {}",
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable().getMessage()))
                .onSuccess(event -> log.info("Voice note processing succeeded after {} attempts",
                        event.getNumberOfRetryAttempts()))
                .onError(event -> log.error("Voice note processing failed after {} attempts",
                        event.getNumberOfRetryAttempts()));

        return retry;
    }
}