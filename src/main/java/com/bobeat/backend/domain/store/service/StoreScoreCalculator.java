package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.SeatOption;
import com.bobeat.backend.domain.store.entity.SeatType;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.repository.MenuRepository;
import com.bobeat.backend.domain.store.repository.SeatOptionRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 식당의 내부 정렬용 가중치 점수를 계산하는 서비스
 *
 * 가중치 계산 기준:
 * - 혼밥레벨이 낮을수록 가산점 (혼밥하기 좋음)
 * - 메인 메뉴 가격이 낮을수록 가산점
 * - 1인석/바좌석이 많을수록 가산점
 * - 카테고리가 패스트푸드/샐러드/카페일수록 가산점
 * - 카테고리가 중식/멕시칸/양식일수록 감점
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreScoreCalculator {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final SeatOptionRepository seatOptionRepository;

    // 가중치 상수
    private static final double MAX_SCORE = 100.0;

    // 혼밥레벨 가중치 (최대 30점)
    private static final double HONBOB_LEVEL_WEIGHT = 30.0;

    // 가격 가중치 (최대 25점)
    private static final double PRICE_WEIGHT = 25.0;
    private static final int PRICE_THRESHOLD_LOW = 8000;  // 8천원 이하면 만점
    private static final int PRICE_THRESHOLD_HIGH = 20000; // 2만원 이상이면 0점

    // 좌석 가중치 (최대 25점)
    private static final double SEAT_WEIGHT = 25.0;

    // 카테고리 가중치 (최대 20점)
    private static final double CATEGORY_WEIGHT = 20.0;

    /**
     * 업데이트가 필요한 식당들의 내부 점수만 계산하고 업데이트 (증분 업데이트)
     * internalScore가 null이거나 scoreUpdateFlag가 true인 경우만 처리
     */
    @Transactional
    public int calculateAndUpdatePendingScores() {
        log.info("Starting incremental score calculation for stores needing update");

        List<Store> stores = storeRepository.findStoresNeedingScoreUpdate();
        int updatedCount = 0;

        for (Store store : stores) {
            try {
                double score = calculateStoreScore(store);
                store.updateInternalScore(score);
                updatedCount++;
            } catch (Exception e) {
                log.error("Failed to calculate score for store id: {}", store.getId(), e);
            }
        }

        if (updatedCount > 0) {
            storeRepository.saveAll(stores);
        }

        log.info("Completed incremental score calculation. Updated {} stores", updatedCount);
        return updatedCount;
    }

    /**
     * 모든 식당의 내부 점수를 강제로 재계산 (전체 업데이트)
     * 내부 정렬 로직이 변경되었을 때 사용
     */
    @Transactional
    public int calculateAndUpdateAllScores() {
        log.info("Starting FULL score recalculation for all stores");

        List<Store> stores = storeRepository.findAll();
        int updatedCount = 0;

        for (Store store : stores) {
            try {
                double score = calculateStoreScore(store);
                store.updateInternalScore(score);
                updatedCount++;
            } catch (Exception e) {
                log.error("Failed to calculate score for store id: {}", store.getId(), e);
            }
        }

        storeRepository.saveAll(stores);
        log.info("Completed FULL score recalculation. Updated {} stores", updatedCount);
        return updatedCount;
    }

    /**
     * 개별 식당의 점수 계산
     */
    public double calculateStoreScore(Store store) {
        double score = 0.0;

        // 1. 혼밥레벨 점수 (레벨이 낮을수록 높은 점수)
        score += calculateHonbobLevelScore(store);

        // 2. 가격 점수 (가격이 낮을수록 높은 점수)
        score += calculatePriceScore(store);

        // 3. 좌석 점수 (1인석/바좌석이 많을수록 높은 점수)
        score += calculateSeatScore(store);

        // 4. 카테고리 점수
        score += calculateCategoryScore(store);

        return Math.min(score, MAX_SCORE);
    }

    /**
     * 혼밥레벨 기반 점수 계산
     * Level 1 (하) -> 30점
     * Level 2 (중) -> 20점
     * Level 3 (중상) -> 10점
     * Level 4 (상) -> 0점
     */
    private double calculateHonbobLevelScore(Store store) {
        if (store.getHonbobLevel() == null) {
            return HONBOB_LEVEL_WEIGHT / 2; // 기본값
        }

        int levelValue = store.getHonbobLevel().getValue();

        return switch (levelValue) {
            case 1 -> HONBOB_LEVEL_WEIGHT;           // 하: 30점
            case 2 -> HONBOB_LEVEL_WEIGHT * 0.67;    // 중: 20점
            case 3 -> HONBOB_LEVEL_WEIGHT * 0.33;    // 중상: 10점
            case 4 -> 0.0;                           // 상: 0점
            default -> HONBOB_LEVEL_WEIGHT / 2;      // 기본값: 15점
        };
    }

    /**
     * 메뉴 가격 기반 점수 계산
     * 대표 메뉴의 최저가를 기준으로 계산
     * 8,000원 이하: 25점
     * 8,000~20,000원: 선형 감소
     * 20,000원 이상: 0점
     */
    private double calculatePriceScore(Store store) {
        List<Menu> menus = menuRepository.findByStore(store);

        if (menus.isEmpty()) {
            return PRICE_WEIGHT / 2; // 메뉴 정보 없으면 중간 점수
        }

        // 대표 메뉴 중 최저가 찾기
        int minPrice = menus.stream()
                .filter(Menu::isRecommend)
                .mapToInt(Menu::getPrice)
                .min()
                .orElse(menus.stream()
                        .mapToInt(Menu::getPrice)
                        .min()
                        .orElse(15000)); // 기본값 15,000원

        if (minPrice <= PRICE_THRESHOLD_LOW) {
            return PRICE_WEIGHT;
        } else if (minPrice >= PRICE_THRESHOLD_HIGH) {
            return 0.0;
        } else {
            // 선형 감소
            double ratio = (double) (PRICE_THRESHOLD_HIGH - minPrice) /
                          (PRICE_THRESHOLD_HIGH - PRICE_THRESHOLD_LOW);
            return PRICE_WEIGHT * ratio;
        }
    }

    /**
     * 좌석 옵션 기반 점수 계산
     * 1인석/바좌석이 있으면 각각 가산점
     * 둘 다 있으면 만점
     */
    private double calculateSeatScore(Store store) {
        List<SeatOption> seatOptions = seatOptionRepository.findByStore(store);

        if (seatOptions.isEmpty()) {
            return 0.0;
        }

        boolean hasForOne = seatOptions.stream()
                .anyMatch(seat -> seat.getSeatType() == SeatType.FOR_ONE);
        boolean hasBarTable = seatOptions.stream()
                .anyMatch(seat -> seat.getSeatType() == SeatType.BAR_TABLE);

        if (hasForOne && hasBarTable) {
            return SEAT_WEIGHT; // 둘 다 있으면 만점
        } else if (hasForOne || hasBarTable) {
            return SEAT_WEIGHT * 0.6; // 하나만 있으면 60%
        } else {
            return SEAT_WEIGHT * 0.2; // 다른 좌석만 있으면 20%
        }
    }

    /**
     * 카테고리 기반 점수 계산
     * 혼밥하기 좋은 카테고리: 패스트푸드, 샐러드, 카페 -> 높은 점수
     * 혼밥하기 어려운 카테고리: 중식, 멕시칸, 양식 -> 낮은 점수
     */
    private double calculateCategoryScore(Store store) {
        if (store.getCategories() == null ||
            store.getCategories().getPrimaryCategory() == null) {
            return CATEGORY_WEIGHT / 2;
        }

        String category = store.getCategories().getPrimaryCategory().getPrimaryType();

        return switch (category) {
            case "패스트푸드", "샐러드", "카페" -> CATEGORY_WEIGHT;        // 20점
            case "한식", "일식", "분식", "아시안", "기타" -> CATEGORY_WEIGHT * 0.6;  // 12점
            case "중식", "멕시칸", "양식" -> CATEGORY_WEIGHT * 0.2;         // 4점
            default -> CATEGORY_WEIGHT / 2;                                // 10점
        };
    }
}