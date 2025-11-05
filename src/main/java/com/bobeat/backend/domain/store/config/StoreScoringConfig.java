package com.bobeat.backend.domain.store.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private List<CategoryWeightItem> categoryWeights = List.of();

    /**
     * 복합 점수 계산 설정 (추천순 정렬용)
     */
    private CompositeScoreConfig composite = new CompositeScoreConfig();

    private Map<String, Double> categoryWeightMap;

    public double getCategoryWeightRatio(String category) {
        if (this.categoryWeightMap == null) {
            this.categoryWeightMap = categoryWeights.stream()
                    .collect(Collectors.toMap(CategoryWeightItem::getName, CategoryWeightItem::getRatio));
        }

        return categoryWeightMap.getOrDefault(category, 0.5);
    }

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

    @Getter
    @Setter
    public static class CategoryWeightItem {
        private String name;
        private Double ratio;
    }

    @Getter
    @Setter
    public static class CompositeScoreConfig {
        /**
         * 내부 점수 가중치 (0.0 ~ 1.0)
         */
        private Double internalScoreWeight = 0.3;

        /**
         * 거리 가중치 (0.0 ~ 1.0)
         */
        private Double distanceWeight = 0.7;

        /**
         * 최대 검색 반경 (미터)
         */
        private Integer maxSearchRadius = 5000;

        /**
         * 내부 점수 기본값 (점수 없을 때)
         */
        private Double defaultInternalScore = 50.0;

        @PostConstruct
        public void validate() {
            double sum = internalScoreWeight + distanceWeight;
            if (Math.abs(sum - 1.0) > 0.001) {
                throw new IllegalStateException(
                    "Internal score weight and distance weight must sum to 1.0, but got: " + sum
                );
            }
        }
    }
}