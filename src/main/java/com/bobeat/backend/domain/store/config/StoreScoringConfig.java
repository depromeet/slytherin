package com.bobeat.backend.domain.store.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 식당 점수 계산 가중치 설정
 * application.yml에서 가중치를 관리하여 유연하게 대응
 */
@Configuration
@ConfigurationProperties(prefix = "store.scoring")
@Getter
@Setter
public class StoreScoringConfig {

    /**
     * 혼밥레벨 가중치 (최대 점수)
     */
    private Double honbobLevelWeight = 30.0;

    /**
     * 가격 가중치 (최대 점수)
     */
    private Double priceWeight = 25.0;

    /**
     * 좌석 타입 가중치 (최대 점수)
     */
    private Double seatTypeWeight = 25.0;

    /**
     * 카테고리 가중치 (최대 점수)
     */
    private Double categoryWeight = 20.0;

    /**
     * 가격 임계값 설정
     */
    private PriceThreshold priceThreshold = new PriceThreshold();

    /**
     * 카테고리별 가중치 비율 (0.0 ~ 1.0)
     * 예: "패스트푸드": 1.0, "중식": 0.2
     */
    private Map<String, Double> categoryWeights = Map.of();

    @Getter
    @Setter
    public static class PriceThreshold {
        /**
         * 이 가격 이하면 만점 (원)
         */
        private Integer low = 8000;

        /**
         * 이 가격 이상이면 0점 (원)
         */
        private Integer high = 20000;
    }

    /**
     * 특정 카테고리의 가중치 비율 반환
     * 설정에 없으면 기본값 0.5 반환
     */
    public double getCategoryWeightRatio(String category) {
        return categoryWeights.getOrDefault(category, 0.5);
    }
}