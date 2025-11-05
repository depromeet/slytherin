package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.store.config.StoreScoringConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 복합 점수 계산 유틸리티
 * 내부 점수와 거리를 정규화하여 가중 합산
 *
 * 기존 문제: 내부점수 * 0.3 - 거리 * 0.0007 방식은 스케일이 다름
 * 개선: 거리를 0~100 점수로 정규화 후 가중 합산
 */
@Component
@RequiredArgsConstructor
public class CompositeScoreCalculator {

    private final StoreScoringConfig scoringConfig;

    /**
     * 복합 점수 계산 (DB 쿼리용 SQL 표현식 생성)
     *
     * compositeScore = (normalizedInternalScore * W1) + (normalizedDistanceScore * W2)
     *
     * - normalizedInternalScore: 0~100 (그대로)
     * - normalizedDistanceScore: 0~5000m을 100~0으로 정규화 (가까울수록 높음)
     * - W1: internalScoreWeight (기본 0.3)
     * - W2: distanceWeight (기본 0.7)
     *
     * 예시:
     * - 내부점수 80, 거리 1000m
     *   - normalizedInternal = 80
     *   - normalizedDistance = (5000-1000)/5000 * 100 = 80
     *   - compositeScore = 80*0.3 + 80*0.7 = 24 + 56 = 80
     *
     * @return SQL 표현식 문자열 (QueryDSL Expressions.numberTemplate에서 사용)
     *         {0} = store.internalScore
     *         {1} = distance expression
     */
    public String buildCompositeScoreExpression() {
        var config = scoringConfig.getComposite();
        double w1 = config.getInternalScoreWeight();
        double w2 = config.getDistanceWeight();
        int maxRadius = config.getMaxSearchRadius();
        double defaultScore = config.getDefaultInternalScore();

        // (COALESCE(internalScore, 50.0) * 0.3) + (((5000 - distance) / 5000.0 * 100.0) * 0.7)
        return String.format(
            "(COALESCE({0}, %.1f) * %.2f) + (((%d - {1}) / %d.0 * 100.0) * %.2f)",
            defaultScore, w1, maxRadius, maxRadius, w2
        );
    }

    /**
     * Java에서 직접 계산 (테스트용)
     */
    public double calculate(Double internalScore, int distanceMeters) {
        var config = scoringConfig.getComposite();
        double w1 = config.getInternalScoreWeight();
        double w2 = config.getDistanceWeight();
        int maxRadius = config.getMaxSearchRadius();
        double defaultScore = config.getDefaultInternalScore();

        double normalizedInternal = internalScore != null ? internalScore : defaultScore;
        double normalizedDistance = Math.max(0, (maxRadius - distanceMeters) / (double) maxRadius * 100.0);

        return (normalizedInternal * w1) + (normalizedDistance * w2);
    }
}