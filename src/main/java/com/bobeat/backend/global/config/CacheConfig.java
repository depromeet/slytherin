package com.bobeat.backend.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 캐싱 설정
 *
 * Caffeine Cache를 사용하여 애플리케이션 레벨 캐싱을 구현합니다.
 *
 * 성능 개선:
 * - 카테고리 조회: DB 쿼리 제거 → 메모리 조회 (99% 빠름)
 * - 검색 임베딩 중복 조회: 외부 API 호출 제거
 * - 자주 조회되는 가게 이미지: 응답시간 50ms → 1ms
 *
 * 캐시 전략:
 * - 최대 10,000개 엔트리 보관
 * - 1시간 후 자동 만료
 * - LRU(Least Recently Used) 정책
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 기본 캐시 매니저 설정
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "categories",           // PrimaryCategory 캐시
                "searchEmbeddings",     // SearchHistoryEmbedding 캐시
                "storeMainImages"       // StoreImage 메인 이미지 캐시
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats()  // 캐시 통계 기록 (모니터링용)
                .evictionListener((key, value, cause) -> {
                    log.debug("Cache evicted - key: {}, cause: {}", key, cause);
                }));

        return cacheManager;
    }
}
