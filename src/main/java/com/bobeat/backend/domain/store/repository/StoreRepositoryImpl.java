package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.QMenu;
import com.bobeat.backend.domain.store.entity.QSeatOption;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.global.util.KeysetCursor;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

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
    public List<StoreRow> findStoresSlice(StoreFilteringRequest request, int limitPlusOne) {
        double centerLat = request.center().lat();
        double centerLon = request.center().lon();

        NumberExpression<Integer> distanceExpr = distanceMeters(store.address.location, centerLat, centerLon);

        BooleanExpression filters = andAll(
                buildLocationFilter(request, centerLat, centerLon),
                levelLoe(request),
                categoriesIn(request),
                recommendedMenuPriceInRange(request),
                seatTypesIn(request)
        );

        filters = andAll(filters, applyKeyset(request, distanceExpr));

        OrderSpecifier<?>[] orders = new OrderSpecifier<?>[]{
                distanceExpr.asc(),
                store.id.asc()
        };

        List<Tuple> rows = queryFactory
                .select(store, distanceExpr)
                .from(store)
                .where(filters)
                .orderBy(orders)
                .limit(limitPlusOne)
                .fetch();

        List<StoreRow> result = new ArrayList<>(rows.size());
        for (Tuple t : rows) {
            Store s = t.get(store);
            Integer d = t.get(distanceExpr);
            result.add(new StoreRow(s, d != null ? d : 0));
        }
        return result;
    }

    private BooleanExpression applyKeyset(StoreFilteringRequest req, NumberExpression<Integer> distanceExpr) {
        if (req.paging() == null) return null;

        var cursor = KeysetCursor.decodeOrNull(req.paging().lastKnown());
        if (cursor == null) return null;

        int  lastDist = cursor.distance();
        long lastId   = cursor.id();

        return distanceExpr.gt(lastDist)
                .or(distanceExpr.eq(lastDist).and(store.id.gt(lastId)));
    }

    @Override
    public Map<Long, StoreSearchResultDto.SignatureMenu> findRepresentativeMenus(List<Long> storeIds) {
        if (storeIds.isEmpty()) return Map.of();

        List<Menu> menus = queryFactory
                .selectFrom(menu)
                .where(menu.store.id.in(storeIds))
                .fetch();

        Map<Long, StoreSearchResultDto.SignatureMenu> map = new LinkedHashMap<>();
        for (Menu m : menus) {
            Long sid = m.getStore().getId();
            if (!map.containsKey(sid)) {
                map.put(sid, new StoreSearchResultDto.SignatureMenu(m.getName(), m.getPrice()));
            }
        }
        return map;
    }

    @Override
    public Map<Long, List<String>> findSeatTypes(List<Long> storeIds) {
        if (storeIds.isEmpty()) return Map.of();

        QSeatOption so = seatOption;

        List<Tuple> rows = queryFactory
                .select(so.store.id, so.seatType)
                .from(so)
                .where(so.store.id.in(storeIds))
                .orderBy(so.store.id.asc(), so.seatType.asc())
                .fetch();

        Map<Long, List<String>> result = new LinkedHashMap<>();
        for (Tuple t : rows) {
            Long sid = t.get(so.store.id);
            String seat = Objects.requireNonNull(t.get(so.seatType)).name();
            result.computeIfAbsent(sid, k -> new ArrayList<>()).add(seat);
        }
        result.replaceAll((k, v) -> v.stream().distinct().toList());
        return result;
    }

    private Map<Long, StoreSearchResultDto.SignatureMenu> loadRepresentativeMenus(List<Long> storeIds) {
        if (storeIds.isEmpty()) return Map.of();

        List<Menu> menus = queryFactory
                .selectFrom(QMenu.menu)
                .where(QMenu.menu.store.id.in(storeIds))
                .orderBy(
                        QMenu.menu.store.id.asc(),
                        QMenu.menu.recommend.desc(),
                        QMenu.menu.price.asc(),
                        QMenu.menu.id.asc()
                )
                .fetch();

        Map<Long, StoreSearchResultDto.SignatureMenu> map = new LinkedHashMap<>();
        for (Menu m : menus) {
            Long sid = m.getStore().getId();
            if (!map.containsKey(sid)) {
                map.put(sid, new StoreSearchResultDto.SignatureMenu(m.getName(), m.getPrice()));
            }
        }
        return map;
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
        if(request.filters() == null || request.filters().honbobLevel() == null) {
            return null;
        }
        Level target = Level.fromValue(request.filters().honbobLevel());
        return store.honbobLevel.loe(target);
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
