package com.bobeat.backend.domain.store.repository;


import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.repository.query.StoreQueryFilterBuilder;
import com.bobeat.backend.domain.store.repository.query.StoreQuerySortBuilder;
import com.bobeat.backend.global.util.KeysetCursor;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.bobeat.backend.domain.common.PostgisExpressions.distanceMeters;
import static com.bobeat.backend.domain.store.entity.QMenu.menu;
import static com.bobeat.backend.domain.store.entity.QSeatOption.seatOption;
import static com.bobeat.backend.domain.store.entity.QStore.store;

/**
 * Store 검색 QueryDSL 구현체
 * Builder 패턴을 적용하여 필터/정렬 로직을 분리
 */
@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final StoreQueryFilterBuilder filterBuilder;
    private final StoreQuerySortBuilder sortBuilder;

    @Override
    public List<StoreRow> findStoresSlice(StoreFilteringRequest request, int limitPlusOne) {
        double centerLat = request.center().lat();
        double centerLon = request.center().lon();

        NumberExpression<Integer> distanceExpr = distanceMeters(store.address.location, centerLat, centerLon);

        // 가격 또는 좌석 필터가 있으면 JOIN 사용, 없으면 일반 쿼리
        if (filterBuilder.needsJoin(request)) {
            return findStoresWithJoins(request, limitPlusOne, distanceExpr, centerLat, centerLon);
        }

        // Builder를 사용한 필터 구축
        BooleanExpression filters = andAll(
                filterBuilder.buildAllFilters(request, centerLat, centerLon),
                applyKeyset(request, distanceExpr)
        );

        // Builder를 사용한 정렬 구축
        OrderSpecifier<?>[] orders = sortBuilder.buildOrderSpecifiers(request, distanceExpr);

        List<Tuple> rows = queryFactory
                .select(store, distanceExpr)
                .from(store)
                .where(filters)
                .orderBy(orders)
                .limit(limitPlusOne)
                .fetch();

        return convertToStoreRows(rows, distanceExpr);
    }

    /**
     * 가격/좌석 필터가 있을 때 2단계 쿼리로 조회
     *
     * 전략:
     * 1단계 - JOIN으로 필터링된 Store ID만 조회 (DISTINCT)
     * 2단계 - ID로 Store + distance 조회 (중복 없음, 정렬 정확)
     *
     * 이유:
     * - 상관 서브쿼리는 각 Store마다 반복 실행되어 느림
     * - 단순 JOIN은 중복 발생 (1 Store : N Menu)
     * - DISTINCT + ORDER BY는 PostgreSQL 에러 발생
     * - GROUP BY 모든 컬럼은 코드가 지저분함
     *
     * 상세 설명: SUBQUERY_OPTIMIZATION.md 참고
     */
    private List<StoreRow> findStoresWithJoins(StoreFilteringRequest request, int limitPlusOne, NumberExpression<Integer> distanceExpr, double centerLat, double centerLon) {
        // 1단계: JOIN으로 필터링된 Store ID만 조회
        List<Long> filteredStoreIds = findFilteredStoreIds(request, centerLat, centerLon, limitPlusOne);

        if (filteredStoreIds.isEmpty()) {
            return List.of();
        }

        // 2단계: ID로 Store + distance 조회 (중복 없음)
        BooleanExpression filters = andAll(
                filterBuilder.buildAllFilters(request, centerLat, centerLon),
                applyKeyset(request, distanceExpr),
                store.id.in(filteredStoreIds)
        );

        OrderSpecifier<?>[] orders = sortBuilder.buildOrderSpecifiers(request, distanceExpr);

        List<Tuple> rows = queryFactory
                .select(store, distanceExpr)
                .from(store)
                .where(filters)
                .orderBy(orders)
                .limit(limitPlusOne)
                .fetch();

        return convertToStoreRows(rows, distanceExpr);
    }

    /**
     * JOIN 조건으로 필터링된 Store ID 목록 조회 (1단계) //TODO: 데이터가 많아져 성능저하 시 고민 필요
     *
     * 전략:
     * - DISTINCT로 중복 제거하여 유효한 모든 Store ID를 조회
     * - LIMIT을 적용하지 않음 → 2단계에서 키셋 페이징으로 정확하게 제어
     * - 이 방식으로 무한 스크롤에서 데이터 누락 방지
     *
     * 트레이드오프:
     * - 장점: 키셋 페이징이 정확하게 동작, 데이터 누락 없음
     * - 단점: 필터링된 Store가 많으면 1단계에서 많은 ID를 가져올 수 있음
     * - 일반적으로 위치 필터(반경 5km)로 인해 결과 집합은 제한적
     */
    private List<Long> findFilteredStoreIds(StoreFilteringRequest request, double centerLat, double centerLon, int limitPlusOne) {
        // 기본 필터
        BooleanExpression baseFilters = filterBuilder.buildAllFilters(request, centerLat, centerLon);

        // JOIN 필터 (Builder를 통해 생성)
        BooleanExpression priceJoinFilter = filterBuilder.buildPriceJoinFilter(request);
        BooleanExpression seatJoinFilter = filterBuilder.buildSeatJoinFilter(request);

        // 쿼리 빌드
        var query = queryFactory
                .select(store.id)
                .distinct()  // Store ID만 조회하므로 DISTINCT 사용 가능
                .from(store);

        // JOIN 추가 (Builder를 통해 확인)
        if (filterBuilder.needsPriceJoin(request)) {
            query = query.join(menu).on(menu.store.eq(store));
        }
        if (filterBuilder.needsSeatJoin(request)) {
            query = query.join(seatOption).on(seatOption.store.eq(store));
        }

        // WHERE 조건
        query = query.where(andAll(baseFilters, priceJoinFilter, seatJoinFilter));

        // LIMIT 없이 모든 유효한 Store ID 조회
        // 2단계에서 키셋 페이징과 정렬을 정확하게 적용
        return query.fetch();
    }

    @Override
    public Map<Long, StoreSearchResultDto.SignatureMenu> findRepresentativeMenus(List<Long> storeIds) {
        if (storeIds.isEmpty()) return Map.of();

        // 대표 메뉴 선택 기준: 추천 메뉴 우선, 가격 낮은 순
        List<Menu> menus = queryFactory
                .selectFrom(menu)
                .where(menu.store.id.in(storeIds))
                .orderBy(
                        menu.store.id.asc(),
                        menu.recommend.desc(),
                        menu.price.asc(),
                        menu.id.asc()
                )
                .fetch();

        return convertToSignatureMenuMap(menus);
    }

    @Override
    public Map<Long, List<String>> findSeatTypes(List<Long> storeIds) {
        if (storeIds.isEmpty()) return Map.of();

        List<Tuple> rows = queryFactory
                .select(seatOption.store.id, seatOption.seatType)
                .from(seatOption)
                .where(seatOption.store.id.in(storeIds))
                .orderBy(seatOption.store.id.asc(), seatOption.seatType.asc())
                .fetch();

        return convertToSeatTypeMap(rows);
    }

    // ==================== Private Helper Methods ====================

    private BooleanExpression applyKeyset(StoreFilteringRequest req, NumberExpression<Integer> distanceExpr) {
        if (req.paging() == null) {
            return null;
        }

        var cursor = KeysetCursor.decodeOrNull(req.paging().lastKnown());
        if (cursor == null) {
            return null;
        }

        int lastDist = cursor.distance();
        long lastId = cursor.id();

        return distanceExpr.gt(lastDist)
                .or(distanceExpr.eq(lastDist).and(store.id.gt(lastId)));
    }

    private List<StoreRow> convertToStoreRows(List<Tuple> tuples, NumberExpression<Integer> distanceExpr) {
        List<StoreRow> result = new ArrayList<>(tuples.size());
        for (Tuple t : tuples) {
            Store s = t.get(store);
            Integer d = t.get(distanceExpr);
            result.add(new StoreRow(s, Objects.requireNonNullElse(d, 0)));
        }
        return result;
    }

    private Map<Long, StoreSearchResultDto.SignatureMenu> convertToSignatureMenuMap(List<Menu> menus) {
        Map<Long, StoreSearchResultDto.SignatureMenu> map = new LinkedHashMap<>();
        for (Menu m : menus) {
            Long sid = m.getStore().getId();
            if (!map.containsKey(sid)) {
                map.put(sid, new StoreSearchResultDto.SignatureMenu(m.getName(), m.getPrice()));
            }
        }
        return map;
    }

    private Map<Long, List<String>> convertToSeatTypeMap(List<Tuple> rows) {
        Map<Long, List<String>> result = new LinkedHashMap<>();
        for (Tuple t : rows) {
            Long sid = t.get(seatOption.store.id);
            String seat = Objects.requireNonNull(t.get(seatOption.seatType)).name();
            result.computeIfAbsent(sid, k -> new ArrayList<>()).add(seat);
        }
        result.replaceAll((k, v) -> v.stream().distinct().toList());
        return result;
    }

    private BooleanExpression andAll(BooleanExpression... exprs) {
        BooleanExpression acc = null;
        for (BooleanExpression e : exprs) {
            if (e == null) {
                continue;
            }
            acc = (acc == null) ? e : acc.and(e);
        }
        return acc;
    }
}