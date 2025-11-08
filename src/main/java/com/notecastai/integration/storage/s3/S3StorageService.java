package com.notecastai.integration.storage.s3;

import com.notecastai.common.exeption.TechnicalException;
import com.notecastai.integration.storage.StorageService;
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
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Client s3;
    private final S3Presigner presigner;

    @Value("${aws.s3.bucket}")
    String bucket;

    private static final Duration PRESIGNED_URL_TTL = Duration.ofHours(24);

    @Override
    public String put(String key, InputStream data, long size, String ct) {
        s3.putObject(
                PutObjectRequest.builder().bucket(bucket).key(key).contentType(ct).build(),
                RequestBody.fromInputStream(data, size)
        );
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
            log.info("Async S3 upload completed: {}", key);
            return CompletableFuture.completedFuture(key);
        } catch (Exception e) {
            log.error("Async S3 upload failed: {}", key, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public String presignedPut(String key, String ct) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        var req = PutObjectRequest.builder().bucket(bucket).key(key).contentType(ct).build();
        var url = presigner.presignPutObject(b -> b.signatureDuration(PRESIGNED_URL_TTL).putObjectRequest(req)).url();
        try {
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
        if (key == null || key.isEmpty()) {
            return null;
        }
        var req = GetObjectRequest.builder().bucket(bucket).key(key).build();
        var url = presigner.presignGetObject(b -> b.signatureDuration(PRESIGNED_URL_TTL).getObjectRequest(req)).url();

        try {
            return url.toString();
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
    }
}