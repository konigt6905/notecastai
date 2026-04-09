package com.notecastai.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {

    private TierConfig tts = new TierConfig(5, 60);           // 5 per minute
    private TierConfig aiGeneration = new TierConfig(10, 60);  // 10 per minute
    private TierConfig aiFormat = new TierConfig(20, 60);      // 20 per minute
    private TierConfig aiChat = new TierConfig(30, 60);        // 30 per minute
    private TierConfig standard = new TierConfig(100, 60);     // 100 per minute

    @Getter
    @Setter
    public static class TierConfig {
        private int capacity;
        private int refillSeconds;

        public TierConfig() {}

        public TierConfig(int capacity, int refillSeconds) {
            this.capacity = capacity;
            this.refillSeconds = refillSeconds;
        }

        public Bucket newBucket() {
            Bandwidth limit = Bandwidth.classic(
                    capacity,
                    Refill.greedy(capacity, Duration.ofSeconds(refillSeconds))
            );
            return Bucket.builder().addLimit(limit).build();
        }
    }
}
