package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.global.response.CursorPageResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.bobeat.backend.domain.store.entity.QStore.store;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Store> search(StoreFilteringRequest request) {
//        return null;
//    }
        int pageSize = request.paging() != null? request.paging().limit() : 20;

        List<Store> results = queryFactory
                .selectFrom(store)
                .leftJoin(store.categories.primaryCategory).fetchJoin()
                .leftJoin(store.categories.secondaryCategory).fetchJoin()
                .where(
                        // Bounding Box 조건
                        bboxWithin(request),
                        // 혼밥 레벨 조건
                        levelLoe(request),
                        // 메뉴 카테고리 조건
                        categoriesIn(request),
//                        // 가격 조건
//                        priceBetween(request),
//                        // 좌석 형태 조건
//                        seatTypesIn(request),
//                        // 결제 방식 조건 (paymentMethodsIn 메서드 구현 필요)
//                        paymentMethodsIn(request)
                )
                .orderBy(store.id.desc())
                .limit(pageSize + 1)
                .fetch();
        return null;
    }

    private BooleanExpression bboxWithin(StoreFilteringRequest request) {
        if(request == null || request.bbox() == null || request.bbox().nw() == null || request.bbox().se() == null) {
            return null;
        }

        Double nwLat = request.bbox().nw().lat();
        Double nwLon = request.bbox().nw().lon();
        Double seLat = request.bbox().se().lat();
        Double seLon = request.bbox().se().lon();

        BooleanExpression latBetween = null;
        if(nwLat != null && seLat != null) {
            latBetween = store.address.latitude.loe(nwLat).and(store.address.latitude.loe(seLat));
        } else if(nwLat != null) {
            latBetween = store.address.latitude.loe(nwLat);
        } else if(seLat != null) {
            latBetween = store.address.latitude.goe(seLat);
        }

        BooleanExpression lonBetween = null;
        if(nwLon != null && seLon != null) {
            lonBetween = store.address.longitude.goe(nwLon).and(store.address.longitude.loe(seLon));
        } else if(nwLon != null) {
            lonBetween = store.address.longitude.goe(nwLon);
        } else if(seLon != null) {
            lonBetween = store.address.longitude.loe(seLon);
        }

        return andAll(latBetween, lonBetween);
    }


    private BooleanExpression levelLoe(StoreFilteringRequest request) {
        if(request.filters() == null || request.filters().level() == null) {
            return null;
        }
        return store.honbobLevel.loe(request.filters().level());
    }

//    private BooleanExpression seatTypesIn(StoreFilteringRequest request) {
//        if (request.filters().seatTypes() == null || request.filters().seatTypes().isEmpty()) {
//            return null;
//        }
//        return store.seatTypes.any().in(request.filters().seatTypes());
//    }
////
//    private BooleanExpression paymentMethodsIn(StoreFilteringRequest request) {
//        if (request.filters().paymentMethods() == null || request.filters().paymentMethods().isEmpty()) {
//            return null;
//        }
//        // TODO: paymentMethods에 대한 EnumType 설정 및 store entity에 반영 필요
//        return store.paymentMethods.any().in(request.filters().paymentMethods());
//    }

    private BooleanExpression categoriesIn(StoreFilteringRequest request) {
        if (request.filters().categories() == null || request.filters().categories().isEmpty()) {
            return null;
        }

        List<String> categories = request.filters().categories();

        BooleanExpression primaryIn = store.categories.primaryCategory.primaryType.in(categories);
        BooleanExpression secondaryIn = store.categories.secondaryCategory.secondaryType.in(categories);

        return primaryIn.or(secondaryIn);
    }

//    private Predicate priceBetween(StoreFilteringRequest request) {
//        if(request.filters() == null || request.filters().price() == null) {
//            return null;
//        }
//        Integer min = request.filters().price().min();
//        Integer max = request.filters().price().max();
//
//        if(min == null && max == null) {
//            return null;
//        } else if(min != null && max != null) {
//            return store..averagePrice.between(min, max);
//        } else if(min != null) {
//            return store.averagePrice.goe(min);
//        } else { // max != null
//            return store.averagePrice.loe(max);
//        }
//    }

    private BooleanExpression andAll(BooleanExpression... exprs) {
        BooleanExpression acc = null;
        for (BooleanExpression e : exprs) {
            if (e == null) continue;
            acc = (acc == null) ? e : acc.and(e);
        }
        return acc;
    }
}
