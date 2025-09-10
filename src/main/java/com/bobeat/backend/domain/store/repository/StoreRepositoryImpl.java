package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.global.response.CursorPageResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {
//    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Store> search(StoreFilteringRequest request) {
        return null;
    }
}
