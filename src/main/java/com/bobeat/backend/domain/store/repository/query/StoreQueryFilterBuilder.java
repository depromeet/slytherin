package com.bobeat.backend.domain.store.repository.query;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.bobeat.backend.domain.common.PostgisExpressions.intersectsEnvelope;
import static com.bobeat.backend.domain.common.PostgisExpressions.stDWithin;
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
    public BooleanExpression buildAllFilters(StoreFilteringRequest request, double centerLat, double centerLon) {
        return andAll(
            buildLocationFilter(request, centerLat, centerLon),
            buildHonbobLevelFilter(request),
            buildCategoryFilter(request)
        );
    }

    /**
     * 위치 필터 (BBox 또는 반경)
     */
    public BooleanExpression buildLocationFilter(StoreFilteringRequest request, double centerLat, double centerLon) {
        if (hasValidBbox(request)) {
            var nw = request.bbox().nw();
            var se = request.bbox().se();
            return intersectsEnvelope(store.address.location, nw.lat(), nw.lon(), se.lat(), se.lon());
        }
        return stDWithin(store.address.location, centerLat, centerLon, DEFAULT_RADIUS_METERS);
    }

    /**
     * 혼밥레벨 필터 - 다중 레벨 지원
     */
    public BooleanExpression buildHonbobLevelFilter(StoreFilteringRequest request) {
        if (request.filters() == null || request.filters().honbobLevel() == null || request.filters().honbobLevel().isEmpty()) {
            return null;
        }
        List<Level> targetLevels = request.filters().honbobLevel().stream()
                .filter(Objects::nonNull)
                .map(Level::fromValue)
                .toList();

        if (targetLevels.isEmpty()) {
            return null;
        }

        return store.honbobLevel.in(targetLevels);
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
     * 가격 필터 존재 여부 확인
     */
    public boolean needsPriceJoin(StoreFilteringRequest request) {
        if (request.filters() == null || request.filters().price() == null) {
            return false;
        }
        var price = request.filters().price();
        return price.min() != null || price.max() != null;
    }

    /**
     * 좌석 타입 필터 존재 여부 확인
     */
    public boolean needsSeatJoin(StoreFilteringRequest request) {
        return request.filters() != null
                && request.filters().seatTypes() != null
                && !request.filters().seatTypes().isEmpty();
    }

    /**
     * JOIN이 필요한 필터가 있는지 확인
     */
    public boolean needsJoin(StoreFilteringRequest request) {
        return needsPriceJoin(request) || needsSeatJoin(request);
    }

    /**
     * 가격 필터를 JOIN 조건으로 빌드
     * 2단계 쿼리의 1단계(Store ID 조회)에서 사용
     *
     * TODO: 임시로 추천 메뉴 조건 제거 - 전체 메뉴 대상으로 가격 필터 적용
     */
    public BooleanExpression buildPriceJoinFilter(StoreFilteringRequest request) {
        if (!needsPriceJoin(request)) {
            return null;
        }

        Integer min = request.filters().price().min();
        Integer max = request.filters().price().max();

        BooleanExpression priceCondition;
        if (min != null && max != null) {
            priceCondition = menu.price.between(min, max);
        } else if (min != null) {
            priceCondition = menu.price.goe(min);
        } else {
            priceCondition = menu.price.loe(max);
        }

        // 추천 메뉴 조건 제거 - 전체 메뉴에서 가격 조건 체크
        return priceCondition;
    }

    /**
     * 좌석 타입 필터를 JOIN 조건으로 빌드
     * 2단계 쿼리의 1단계(Store ID 조회)에서 사용
     */
    public BooleanExpression buildSeatJoinFilter(StoreFilteringRequest request) {
        if (!needsSeatJoin(request)) {
            return null;
        }
        return seatOption.seatType.in(request.filters().seatTypes());
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