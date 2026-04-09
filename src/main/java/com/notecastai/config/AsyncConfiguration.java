package com.notecastai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean(name = "voiceNoteProcessingExecutor")
    public Executor voiceNoteProcessingExecutor() {
        ThreadPoolTaskExecutor executor = createExecutor(5, 10, 100, "voice-note-");

        log.info("Voice note processing executor initialized with core pool size: {}, max pool size: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    @Bean(name = "storageUploadExecutor")
    public Executor storageUploadExecutor() {
        ThreadPoolTaskExecutor executor = createExecutor(3, 8, 50, "storage-upload-");

        log.info("Storage upload executor initialized with core pool size: {}, max pool size: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    @Bean(name = "noteCastProcessingExecutor")
    public Executor noteCastProcessingExecutor() {
        return createExecutor(3, 8, 50, "notecast-processing-");
    }

    @Bean(name = "gameNoteProcessingExecutor")
    public Executor gameNoteProcessingExecutor() {
        return createExecutor(3, 8, 50, "gamenote-processing-");
    }

    private ThreadPoolTaskExecutor createExecutor(int coreSize, int maxSize, int queueCapacity, String prefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(prefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setTaskDecorator(new UserContextTaskDecorator());
        executor.initialize();
        return executor;
    }
}
