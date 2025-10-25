package com.bobeat.backend.domain.store.scheduler;

import com.bobeat.backend.domain.store.service.StoreScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 식당 내부 점수를 주기적으로 계산하는 스케줄러
ㅍ * 매일 자정(00:00)에 업데이트가 필요한 식당들만 증분 업데이트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StoreScoreScheduler {

    private final StoreScoreCalculator storeScoreCalculator;

    /**
     * 매일 자정에 실행 - 증분 업데이트만 수행
     * internalScore가 null이거나 scoreUpdateFlag가 true인 경우만 업데이트
     * cron: 초 분 시 일 월 요일
     * "0 0 0 * * *" = 매일 00시 00분 00초
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void calculatePendingStoreScores() {
        log.info("Starting scheduled incremental store score calculation");

        try {
            int updatedCount = storeScoreCalculator.calculateAndUpdatePendingScores();
            log.info("Successfully completed scheduled score calculation. Updated {} stores", updatedCount);
        } catch (Exception e) {
            log.error("Error during scheduled store score calculation", e);
        }
    }

    /**
     * 서버 시작 시 1분 후에 한 번 실행 - 증분 업데이트
     * 초기에 점수가 없는 식당들만 계산
     */
    @Scheduled(initialDelay = 60000, fixedDelay = Long.MAX_VALUE)
    public void calculateStoreScoresOnStartup() {
        log.info("Starting initial store score calculation on application startup");

        try {
            int updatedCount = storeScoreCalculator.calculateAndUpdatePendingScores();
            log.info("Successfully completed initial score calculation. Updated {} stores", updatedCount);
        } catch (Exception e) {
            log.error("Error during initial store score calculation", e);
        }
    }
}