package com.notecastai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.notecastai.common.api.ErrorResponse;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;

    // userId -> tier -> bucket. In-memory only, won't work across instances.
    // TODO replace with bucket4j-redis
    private final Cache<String, Map<RateLimitTier, Bucket>> userBuckets;

    public RateLimitFilter(RateLimitConfig rateLimitConfig, ObjectMapper objectMapper) {
        this.rateLimitConfig = rateLimitConfig;
        this.objectMapper = objectMapper;
        this.userBuckets = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .maximumSize(10_000)
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/health")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId = extractUserId();
        if (userId == null) {
            // not authenticated, Spring Security will reject it after us
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitTier tier = resolveTier(request.getMethod(), request.getRequestURI());
        Bucket bucket = resolveBucket(userId, tier);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000 + 1;
            log.warn("Rate limit exceeded for user={}, tier={}, retryAfter={}s", userId, tier, retryAfterSeconds);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setHeader("X-Rate-Limit-Remaining", "0");

            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.TOO_MANY_REQUESTS.value())
                    .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
                    .message("Rate limit exceeded. Please retry after " + retryAfterSeconds + " seconds.")
                    .path(request.getRequestURI())
                    .code("RATE_LIMIT_EXCEEDED")
                    .build();

            objectMapper.writeValue(response.getOutputStream(), error);
        }
    }

    private RateLimitTier resolveTier(String method, String uri) {
        // notecast creation does transcript + TTS + S3 upload, cap it hardest
        if ("POST".equals(method) && uri.matches("/api/v1/notecasts/?")) {
            return RateLimitTier.TTS;
        }

        if ("POST".equals(method) && uri.matches("/api/v1/game-notes/?")) {
            return RateLimitTier.AI_GENERATION;
        }

        if ("POST".equals(method) && uri.matches("/api/v1/voice-notes/?")) {
            return RateLimitTier.AI_GENERATION;
        }

        if ("PUT".equals(method) && uri.matches("/api/v1/notes/\\d+/format/?")) {
            return RateLimitTier.AI_FORMAT;
        }
        if ("PUT".equals(method) && uri.matches("/api/v1/notes/\\d+/knowledge/format/?")) {
            return RateLimitTier.AI_FORMAT;
        }

        if ("POST".equals(method) && uri.matches("/api/v1/notes/?")) {
            return RateLimitTier.AI_FORMAT;
        }
        if ("POST".equals(method) && uri.matches("/api/v1/notes/combine/?")) {
            return RateLimitTier.AI_FORMAT;
        }

        if ("POST".equals(method) && uri.matches("/api/v1/notes/\\d+/ask/?")) {
            return RateLimitTier.AI_CHAT;
        }

        return RateLimitTier.STANDARD;
    }

    private Bucket resolveBucket(String userId, RateLimitTier tier) {
        Map<RateLimitTier, Bucket> tierBuckets = userBuckets.get(userId,
                k -> new ConcurrentHashMap<>());
        return tierBuckets.computeIfAbsent(tier, this::createBucket);
    }

    private Bucket createBucket(RateLimitTier tier) {
        return switch (tier) {
            case TTS -> rateLimitConfig.getTts().newBucket();
            case AI_GENERATION -> rateLimitConfig.getAiGeneration().newBucket();
            case AI_FORMAT -> rateLimitConfig.getAiFormat().newBucket();
            case AI_CHAT -> rateLimitConfig.getAiChat().newBucket();
            case STANDARD -> rateLimitConfig.getStandard().newBucket();
        };
    }

    private String extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken token) {
            return token.getToken().getSubject();
        }
        return null;
    }

    enum RateLimitTier {
        TTS,
        AI_GENERATION,
        AI_FORMAT,
        AI_CHAT,
        STANDARD
    }
}
