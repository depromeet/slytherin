package com.bobeat.backend.domain.store.repository.query;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.service.CompositeScoreCalculator;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.bobeat.backend.domain.store.entity.QStore.store;

/**
 * Store 검색 정렬 조건 빌더
 * QueryDSL OrderSpecifier를 생성하는 책임만 가짐
 */
@Component
@RequiredArgsConstructor
public class StoreQuerySortBuilder {

    private final CompositeScoreCalculator compositeScoreCalculator;

    /**
     * sortBy에 따른 정렬 기준 생성
     */
    public OrderSpecifier<?>[] buildOrderSpecifiers(StoreFilteringRequest request, NumberExpression<Integer> distanceExpr) {
        if (request.sortBy() == null || request.sortBy() == StoreFilteringRequest.SortBy.DISTANCE) {
            return buildDistanceSort(distanceExpr);
        }
        return buildRecommendedSort(distanceExpr);
    }

    /**
     * 거리순 정렬 (100% 거리)
     */
    private OrderSpecifier<?>[] buildDistanceSort(NumberExpression<Integer> distanceExpr) {
        return new OrderSpecifier<?>[]{
            distanceExpr.asc(),
            store.id.asc()
        };
    }

    /**
     * 추천순 정렬 (30% 내부점수 + 70% 거리)
     *
     * 개선: 거리를 0~100 점수로 정규화 후 가중 합산
     * compositeScore = (internalScore * 0.3) + (distanceScore * 0.7)
     * distanceScore = (5000 - distance) / 5000 * 100
     */
    private OrderSpecifier<?>[] buildRecommendedSort(NumberExpression<Integer> distanceExpr) {
        // CompositeScoreCalculator에서 SQL 표현식 생성
        NumberExpression<Double> compositeScore = Expressions.numberTemplate(
            Double.class,
            compositeScoreCalculator.buildCompositeScoreExpression(),
            store.internalScore,
            distanceExpr
        );

        return new OrderSpecifier<?>[]{
            compositeScore.desc(),
            store.id.asc()
        };
    }
}