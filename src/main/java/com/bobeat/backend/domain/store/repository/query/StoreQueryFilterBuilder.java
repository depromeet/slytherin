package com.bobeat.backend.domain.store.repository.query;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.bobeat.backend.domain.common.PostgisExpressions.*;
import static com.bobeat.backend.domain.store.entity.QMenu.menu;
import static com.bobeat.backend.domain.store.entity.QSeatOption.seatOption;
import static com.bobeat.backend.domain.store.entity.QStore.store;

/**
 * Store 검색 필터 조건 빌더
 * QueryDSL BooleanExpression을 생성하는 책임만 가짐
 */
@Component
@RequiredArgsConstructor
public class StoreQueryFilterBuilder {

    private static final int DEFAULT_RADIUS_METERS = 5000;

    /**
     * 모든 필터 조합
     */
    public BooleanExpression buildAllFilters(StoreFilteringRequest request,
                                              double centerLat,
                                              double centerLon) {
        return andAll(
            buildLocationFilter(request, centerLat, centerLon),
            buildHonbobLevelFilter(request),
            buildCategoryFilter(request)
        );
    }

    /**
     * 위치 필터 (BBox 또는 반경)
     */
    public BooleanExpression buildLocationFilter(StoreFilteringRequest request,
                                                   double centerLat,
                                                   double centerLon) {
        if (hasValidBbox(request)) {
            var nw = request.bbox().nw();
            var se = request.bbox().se();
            return intersectsEnvelope(store.address.location, nw.lat(), nw.lon(), se.lat(), se.lon());
        }
        return stDWithin(store.address.location, centerLat, centerLon, DEFAULT_RADIUS_METERS);
    }

    /**
     * 혼밥레벨 필터
     */
    public BooleanExpression buildHonbobLevelFilter(StoreFilteringRequest request) {
        if (request.filters() == null || request.filters().honbobLevel() == null) {
            return null;
        }
        Level target = Level.fromValue(request.filters().honbobLevel());
        return store.honbobLevel.loe(target);
    }

    /**
     * 카테고리 필터
     */
    public BooleanExpression buildCategoryFilter(StoreFilteringRequest request) {
        if (request.filters() == null
            || request.filters().categories() == null
            || request.filters().categories().isEmpty()) {
            return null;
        }
        List<String> categories = request.filters().categories();
        return store.categories.primaryCategory.primaryType.in(categories);
    }

    /**
     * 가격 필터 (서브쿼리 방식)
     * TODO: 성능 개선을 위해 JOIN 방식으로 전환 고려
     */
    public BooleanExpression buildPriceFilter(StoreFilteringRequest request) {
        if (request.filters() == null || request.filters().price() == null) {
            return null;
        }
        Integer min = request.filters().price().min();
        Integer max = request.filters().price().max();

        if (min == null && max == null) {
            return null;
        }

        BooleanExpression priceCondition;
        if (min != null && max != null) {
            priceCondition = menu.price.between(min, max);
        } else if (min != null) {
            priceCondition = menu.price.goe(min);
        } else {
            priceCondition = menu.price.loe(max);
        }

        return JPAExpressions
            .selectOne()
            .from(menu)
            .where(
                menu.store.eq(store),
                menu.recommend.isTrue(),
                priceCondition
            )
            .exists();
    }

    /**
     * 좌석 타입 필터 (서브쿼리 방식)
     * TODO: 성능 개선을 위해 JOIN 방식으로 전환 고려
     */
    public BooleanExpression buildSeatTypeFilter(StoreFilteringRequest request) {
        if (request.filters() == null
            || request.filters().seatTypes() == null
            || request.filters().seatTypes().isEmpty()) {
            return null;
        }

        var types = request.filters().seatTypes();
        return JPAExpressions
            .selectOne()
            .from(seatOption)
            .where(
                seatOption.store.eq(store),
                seatOption.seatType.in(types)
            )
            .exists();
    }

    // ==================== Helper Methods ====================

    private boolean hasValidBbox(StoreFilteringRequest req) {
        if (req == null || req.bbox() == null || req.bbox().nw() == null || req.bbox().se() == null) {
            return false;
        }
        var nw = req.bbox().nw();
        var se = req.bbox().se();
        return nw.lat() != null && nw.lon() != null && se.lat() != null && se.lon() != null;
    }

    private BooleanExpression andAll(BooleanExpression... exprs) {
        BooleanExpression acc = null;
        for (BooleanExpression e : exprs) {
            if (e == null) continue;
            acc = (acc == null) ? e : acc.and(e);
        }
        return acc;
    }
}