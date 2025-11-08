package com.notecastai.integration.storage.s3;

import com.notecastai.common.exeption.TechnicalException;
import com.notecastai.integration.storage.StorageService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.InputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Client s3;
    private final S3Presigner presigner;
    private final Clock clock = Clock.systemUTC();

    @Value("${aws.s3.bucket}")
    String bucket;

    // how long the presigned URL is valid
    private static final Duration PRESIGNED_URL_TTL = Duration.ofHours(24);
    // minimal remaining lifetime we accept when reusing from cache
    private static final Duration MIN_REMAINING_TTL = Duration.ofMinutes(2);

    private final Cache<String, Presigned> getCache = Caffeine.newBuilder()
            .expireAfterWrite(PRESIGNED_URL_TTL.minus(MIN_REMAINING_TTL))
            .maximumSize(10_000)
            .build();

    @Override
    public String put(String key, InputStream data, long size, String ct) {
        s3.putObject(
                PutObjectRequest.builder().bucket(bucket).key(key).contentType(ct).build(),
                RequestBody.fromInputStream(data, size)
        );
        getCache.invalidate(key);
        return key;
    }

    @Override
    @Async("storageUploadExecutor")
    public CompletableFuture<String> putAsync(String key, InputStream data, long size, String ct) {
        log.info("Starting async S3 upload: {}", key);
        try {
            s3.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key).contentType(ct).build(),
                    RequestBody.fromInputStream(data, size)
            );
            // object changed -> cached GET URL may be stale; drop it
            getCache.invalidate(key);
            log.info("Async S3 upload completed: {}", key);
            return CompletableFuture.completedFuture(key);
        } catch (Exception e) {
            log.error("Async S3 upload failed: {}", key, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public String presignedPut(String key, String ct) {
        if (key == null || key.isEmpty()) return null;
        var req = PutObjectRequest.builder().bucket(bucket).key(key).contentType(ct).build();
        var url = presigner.presignPutObject(b -> b.signatureDuration(PRESIGNED_URL_TTL).putObjectRequest(req)).url();
        try {
            // usually you want the full URL, not just path;
            // return url.toString() if the client needs the complete link
            return url.getPath();
        } catch (Exception ex) {
            throw TechnicalException.of(TechnicalException.Code.S3_ERROR)
                    .with("key", key)
                    .cause(ex)
                    .build();
        }
    }

    @Override
    public String presignedGet(String key) {
        if (key == null || key.isEmpty()) return null;

        // try cache first
        Presigned cached = getCache.getIfPresent(key);
        if (cached != null && cached.expiresAt.isAfter(Instant.now(clock).plus(MIN_REMAINING_TTL))) {
            return cached.url;
        }

        // generate new one
        var req = GetObjectRequest.builder().bucket(bucket).key(key).build();
        var presigned = presigner.presignGetObject(b -> b
                .signatureDuration(PRESIGNED_URL_TTL)
                .getObjectRequest(req));

        try {
            String url = presigned.url().toString();
            Presigned entry = new Presigned(url, Instant.now(clock).plus(PRESIGNED_URL_TTL));
            getCache.put(key, entry);
            return url;
        } catch (Exception ex) {
            throw TechnicalException.of(TechnicalException.Code.S3_ERROR)
                    .with("key", key)
                    .cause(ex)
                    .build();
        }
    }

    @Override
    public void delete(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        getCache.invalidate(key);
    }

    @Data
    @AllArgsConstructor
    private static class Presigned {
        String url;
        Instant expiresAt;
    }
}