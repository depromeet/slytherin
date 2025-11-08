package com.bobeat.backend.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


/**
 * 비동기 처리 설정
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 기본 비동기 작업용 스레드 풀
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler((r, exec) -> {
            log.warn("비동기 작업 거부 - 큐가 가득 참: {}", r.toString());
        });
        executor.initialize();

        return executor;
    }

    /**
     * 임베딩 생성 전용 스레드 풀 (서버 CPU 코어 기반 설정)
     */
    @Bean(name = "embeddingTaskExecutor")
    public Executor embeddingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("embedding-async-");
        executor.setRejectedExecutionHandler((r, exec) -> {
            log.warn("임베딩 작업 거부 - 큐가 가득 참: {}", r.toString());
        });
        executor.initialize();
        return executor;
    }
}
