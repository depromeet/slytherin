package com.bobeat.backend.domain.store.config;

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
}