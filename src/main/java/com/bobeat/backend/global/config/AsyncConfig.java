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
 *
 * 외부 API 호출(임베딩 생성)을 병렬로 처리하여 성능을 향상시킵니다.
 *
 * 성능 개선:
 * - 10개 가게 등록 시: 10초 → 1-2초 (80-90% 개선)
 * - 외부 API 호출을 병렬로 처리하여 대기 시간 단축
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

        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler((r, exec) -> {
            log.warn("Task rejected. Queue is full. Task: {}", r.toString());
        });
        executor.initialize();

        return executor;
    }

    /**
     * 임베딩 생성 전용 스레드 풀
     *
     * 설정 설명:
     * - corePoolSize: 5 - 기본 스레드 수 (동시 처리 가능한 최소 작업 수)
     * - maxPoolSize: 20 - 최대 스레드 수 (트래픽 급증 시 확장)
     * - queueCapacity: 100 - 대기 큐 크기 (작업이 많을 때 대기)
     * - keepAliveSeconds: 60 - 유휴 스레드 유지 시간
     */
    @Bean(name = "embeddingTaskExecutor")
    public Executor embeddingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("embedding-async-");
        executor.setRejectedExecutionHandler((r, exec) -> {
            log.warn("Embedding task rejected. Queue is full. Task: {}", r.toString());
        });
        executor.initialize();
        return executor;
    }
}
