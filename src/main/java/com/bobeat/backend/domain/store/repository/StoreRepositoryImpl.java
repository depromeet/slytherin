package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.global.response.CursorPageResponse;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.bobeat.backend.domain.common.PostgisExpressions.*;
import static com.bobeat.backend.domain.store.entity.QMenu.menu;
import static com.bobeat.backend.domain.store.entity.QSeatOption.seatOption;
import static com.bobeat.backend.domain.store.entity.QStore.store;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private static final int DEFAULT_RADIUS_METERS = 700;

    @Override
    public CursorPageResponse<Store> search(StoreFilteringRequest request) {
        int pageSize = request.paging() != null? request.paging().limit() : 20;

        double centerLat = request.center().lat();
        double centerLon = request.center().lon();

        NumberExpression<Integer> distanceExpr = distanceMeters(store.address.location, centerLat, centerLon);
        BooleanExpression location = buildLocationFilter(request, centerLat, centerLon);

        BooleanExpression filters = andAll(
                // bounding box + 반경 필터
                location,
                // 레벨 필터
                levelLoe(request),
                // 카테고리 필터
                categoriesIn(request),
                // 추천메뉴 가격 필터
                recommendedMenuPriceInRange(request),
                // 좌석 형태 필터
                seatTypesIn(request)
        );

        OrderSpecifier<?>[] orders = buildOrder(distanceExpr);

        List<Store> results = queryFactory
                .selectFrom(store)
                .leftJoin(store.categories.primaryCategory).fetchJoin()
                .leftJoin(store.categories.secondaryCategory).fetchJoin()
                .where(filters)
                .orderBy(orders)
                .limit(pageSize + 1)
                .fetch();

        return CursorPageResponse.of(results, pageSize, Store::getId);
    }

    private OrderSpecifier<?>[] buildOrder(NumberExpression<Integer> distanceExpr) {
        return new OrderSpecifier<?>[]{
                distanceExpr.asc(),
                store.id.desc()
        };
    }

    private BooleanExpression buildLocationFilter(StoreFilteringRequest req, double centerLat, double centerLon) {
        // 1) 유효한 BBox면 BBox만 적용
        if (hasValidBbox(req)) {
            var nw = req.bbox().nw();
            var se = req.bbox().se();
            return intersectsEnvelope(store.address.location, nw.lat(), nw.lon(), se.lat(), se.lon());
        }
        // 2) BBox 없고, center만 있으면 반경 필터 적용
        return stDWithin(store.address.location, centerLat, centerLon, DEFAULT_RADIUS_METERS);
    }

    private boolean hasValidBbox(StoreFilteringRequest req) {
        if (req == null || req.bbox() == null || req.bbox().nw() == null || req.bbox().se() == null) return false;
        var nw = req.bbox().nw();
        var se = req.bbox().se();
        return nw.lat() != null && nw.lon() != null && se.lat() != null && se.lon() != null;
    }

    private BooleanExpression levelLoe(StoreFilteringRequest request) {
        if(request.filters() == null || request.filters().level() == null) {
            return null;
        }
        return store.honbobLevel.loe(request.filters().level());
    }

    private BooleanExpression categoriesIn(StoreFilteringRequest request) {
        if (request.filters().categories() == null || request.filters().categories().isEmpty()) {
            return null;
        }

        List<String> categories = request.filters().categories();

        BooleanExpression primaryIn = store.categories.primaryCategory.primaryType.in(categories);
        BooleanExpression secondaryIn = store.categories.secondaryCategory.secondaryType.in(categories);

        return primaryIn.or(secondaryIn);
    }

    // TODO: 가격 범위 쿼리 방식 재검토(AS-IS: 서브쿼리 방식으로 추천메뉴 중 가격 조건에 맞는 메뉴가 하나라도 있는지 확인)
    private BooleanExpression recommendedMenuPriceInRange(StoreFilteringRequest request) {
        if(request.filters() == null || request.filters().price() == null) {
            return null;
        }
        Integer min = request.filters().price().min();
        Integer max = request.filters().price().max();

        if(min == null && max == null) {
            return null;
        }
        BooleanExpression priceCondition;

        if(min != null && max != null) {
            priceCondition = menu.price.between(min, max);
        } else if(min != null) {
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

    private BooleanExpression seatTypesIn(StoreFilteringRequest request) {
        if (request.filters().seatTypes() == null || request.filters().seatTypes().isEmpty()) {
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

    private BooleanExpression andAll(BooleanExpression... exprs) {
        BooleanExpression acc = null;
        for (BooleanExpression e : exprs) {
            if (e == null) continue;
            acc = (acc == null) ? e : acc.and(e);
        }
        return acc;
    }
}
