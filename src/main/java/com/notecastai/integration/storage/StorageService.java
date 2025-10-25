package com.notecastai.integration.storage;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

public interface StorageService {
    String put(String key, InputStream data, long size, String contentType);

    URI presignedPut(String key, String contentType, Duration ttl) throws URISyntaxException;

    URI presignedGet(String key, Duration ttl);

    void delete(String key);
}