package com.notecastai.integration.storage;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

public interface StorageService {
    String put(String key, InputStream data, long size, String contentType);

    String presignedPut(String key, String contentType) throws URISyntaxException;

    CompletableFuture<String> putAsync(String key, InputStream data, long size, String contentType);

    String presignedGet(String key);

    void delete(String key);
}