package com.bobeat.backend.domain.store.repository;

import static com.bobeat.backend.domain.store.entity.QStore.store;

import com.bobeat.backend.domain.store.entity.EmbeddingStatus;
import com.bobeat.backend.domain.store.entity.QStore;
import com.bobeat.backend.domain.store.entity.QStoreEmbedding;
import com.bobeat.backend.domain.store.entity.Store;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 유사 가게 추천 Repository 구현체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SimilarStoreRepositoryImpl implements SimilarStoreRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * PostGIS로 특정 가게로부터 지정된 거리 이내에 있는 가게 ID 목록 반환
     *
     * @param storeId           기준 가게 ID
     * @param maxDistanceMeters 최대 거리 (미터 단위)
     *
     * @return 거리 이내에 있는 가게 ID 목록
     */
    @Override
    public List<Long> findNearbyStoreIds(Long storeId, double maxDistanceMeters) {
        QStore targetStore = new QStore("targetStore");
        BooleanExpression distanceFilter = Expressions.booleanTemplate(
                "function('ST_DWithin', {0}, {1}, {2}) = true",
                store.address.location,
                targetStore.address.location,
                maxDistanceMeters
        );
        return queryFactory
                .select(store.id)
                .from(store)
                .innerJoin(targetStore).on(targetStore.id.eq(storeId))
                .where(
                        store.id.ne(storeId),
                        store.address.location.isNotNull(),
                        targetStore.address.location.isNotNull(),
                        distanceFilter,
                        store.honbobLevel.loe(targetStore.honbobLevel)
                )
                .fetch();
    }

    /**
     * 후보 가게들 중에서 기준 가게와 임베딩 벡터 유사도(코사인)가 높은 순으로 정렬하여 반환
     *
     * @param storeId           기준 가게 ID
     * @param candidateStoreIds 후보 가게 ID 목록
     * @param limit             반환할 최대 개수
     *
     * @return 임베딩 유사도 순으로 정렬된 Store 목록
     */
    public List<Store> findSimilarByEmbedding(Long storeId, List<Long> candidateStoreIds, int limit) {
        if (candidateStoreIds == null || candidateStoreIds.isEmpty()) {
            return List.of();
        }

        // se: 후보군 가게의 임베딩
        // targetSe: 비교 기준이 되는 가게(storeId)의 임베딩
        QStoreEmbedding se = new QStoreEmbedding("se");
        QStoreEmbedding targetSe = new QStoreEmbedding("targetSe");

        NumberExpression<Float> semanticDistance = Expressions.numberTemplate(
                Float.class,
                "cosine_distance({0}, {1})",
                se.embedding,
                targetSe.embedding
        );

        return queryFactory
                .select(store)
                .from(store)
                .innerJoin(se).on(store.id.eq(se.store.id))
                .innerJoin(targetSe).on(targetSe.store.id.eq(storeId))
                .where(
                        store.id.in(candidateStoreIds),
                        Expressions.booleanTemplate("{0} is not null", se.embedding),
                        Expressions.booleanTemplate("{0} is not null", targetSe.embedding),
                        se.embeddingStatus.eq(EmbeddingStatus.COMPLETED),
                        targetSe.embeddingStatus.eq(EmbeddingStatus.COMPLETED)
                )
                .orderBy(semanticDistance.asc())
                .limit(limit)
                .fetch();
    }
}
