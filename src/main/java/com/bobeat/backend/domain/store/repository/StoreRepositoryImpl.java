package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.QMenu;
import com.bobeat.backend.domain.store.entity.QSeatOption;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.vo.Categories;
import com.bobeat.backend.global.response.CursorPageResponse;
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
    public List<StoreSearchResultDto> search(StoreFilteringRequest request) {
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

        List<Tuple> rows = queryFactory
                .select(store, distanceExpr)
                .from(store)
                .where(filters)
                .orderBy(orders)
                .limit(pageSize + 1)
                .fetch();

        List<Store> stores = rows.stream().map(r -> r.get(store)).toList();
        List<Long> storeIds = stores.stream().map(Store::getId).toList();
        Map<Long, Integer> distanceById = new HashMap<>();
        for (Tuple t : rows) {
            Store s = t.get(store);
            Integer d = t.get(distanceExpr);
            distanceById.put(s.getId(), d != null ? d : 0);
        }
        boolean hasNext = stores.size() > pageSize;
        if (hasNext) {
            stores = stores.subList(0, pageSize);
            storeIds = stores.stream().map(Store::getId).toList();
        }

        // 4) 2차: 대표메뉴/좌석타입 맵 로딩
        Map<Long, StoreSearchResultDto.SignatureMenu> repMenu = loadRepresentativeMenus(storeIds);
        Map<Long, List<String>> seatTypes = loadSeatTypes(storeIds);

        // 5) DTO 조립 (원본 순서 보존)
        List<StoreSearchResultDto> data = new ArrayList<>(stores.size());
        for (Store s : stores) {
            long id = s.getId();
            int distance = distanceById.getOrDefault(id, 0);
            int walkingMinutes = (int) Math.ceil(distance / 80.0); // 80m/min

            List<String> tags = buildTagsFromCategories(s.getCategories());

            StoreSearchResultDto dto = new StoreSearchResultDto(
                    s.getId(),
                    s.getName(),
                    s.getMainImageUrl(),
                    repMenu.getOrDefault(id, new StoreSearchResultDto.SignatureMenu(null, 0)),
                    distance,
                    walkingMinutes,
                    seatTypes.getOrDefault(id, List.of()),
                    tags,
                    s.getHonbobLevel() != null ? s.getHonbobLevel() : 0
            );
            data.add(dto);
        }

//        Long nextCursor = hasNext ? data.get(data.size() - 1).id() : null;
        return data;
//        return new CursorPageResponse<>(data, nextCursor, hasNext, null);
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

    private Map<Long, List<String>> loadSeatTypes(List<Long> storeIds) {
        if (storeIds.isEmpty()) return Map.of();

        QSeatOption so = QSeatOption.seatOption;

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

    private List<String> buildTagsFromCategories(Categories c) {
        if (c == null) return List.of();
        List<String> tags = new ArrayList<>(2);
        if (c.getPrimaryCategory() != null)   tags.add(c.getPrimaryCategory().toString());
        if (c.getSecondaryCategory() != null) tags.add(c.getSecondaryCategory().toString());
        return tags;
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
