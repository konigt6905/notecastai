package com.notecastai.integration.storage.s3;

import com.notecastai.common.exeption.TechnicalException;
import com.notecastai.integration.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Client s3;
    private final S3Presigner presigner;

    @Value("${app.s3.bucket}") String bucket;

    @Override
    public String put(String key, InputStream data, long size, String ct) {
        // Let AWS SDK exceptions bubble up (they're already runtime) — you asked to handle only URL→URI
        s3.putObject(
                PutObjectRequest.builder().bucket(bucket).key(key).contentType(ct).build(),
                RequestBody.fromInputStream(data, size)
        );
        return key;
    }

    @Override
    public URI presignedPut(String key, String ct, Duration ttl) {
        var req = PutObjectRequest.builder().bucket(bucket).key(key).contentType(ct).build();
        var url = presigner.presignPutObject(b -> b.signatureDuration(ttl).putObjectRequest(req)).url();
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw TechnicalException.of(TechnicalException.Code.STORAGE_PRESIGN_URI_INVALID)
                    .with("key", key)
                    .cause(ex)
                    .build();
        }
    }

    @Override
    public URI presignedGet(String key, Duration ttl) {
        var req = GetObjectRequest.builder().bucket(bucket).key(key).build();
        var url = presigner.presignGetObject(b -> b.signatureDuration(ttl).getObjectRequest(req)).url();
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw TechnicalException.of(TechnicalException.Code.STORAGE_PRESIGN_URI_INVALID)
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