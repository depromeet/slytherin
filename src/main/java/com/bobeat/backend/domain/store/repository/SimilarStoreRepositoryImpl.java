package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.Store;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 유사 가게 추천 Repository 구현체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SimilarStoreRepositoryImpl implements SimilarStoreRepository {

    private final EntityManager entityManager;

    /**
     * PostGIS로 특정 가게로부터 지정된 거리 이내에 있는 가게 ID 목록 반환
     *
     * @param storeId 기준 가게 ID
     * @param maxDistanceMeters 최대 거리 (미터 단위)
     * @return 거리 이내에 있는 가게 ID 목록
     */
    @Override
    public List<Long> findNearbyStoreIds(Long storeId, double maxDistanceMeters) {
        String sql = """
            SELECT s.id
            FROM store s
            INNER JOIN store target ON target.id = :storeId
            WHERE s.id != :storeId
              AND s.location IS NOT NULL
              AND target.location IS NOT NULL
              AND ST_DWithin(
                  s.location::geography,
                  target.location::geography,
                  :maxDistance
              )
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("storeId", storeId);
        query.setParameter("maxDistance", maxDistanceMeters);

        @SuppressWarnings("unchecked")
        List<Long> storeIds = ((List<Object>) query.getResultList())
                .stream()
                .map(id -> ((Number) id).longValue())
                .toList();

        return storeIds;
    }

    /**
     * 후보 가게들 중에서 기준 가게와 임베딩 벡터 유사도가 높은 순으로 정렬하여 반환
     *
     * @param storeId 기준 가게 ID
     * @param candidateStoreIds 후보 가게 ID 목록
     * @param limit 반환할 최대 개수
     * @return 임베딩 유사도 순으로 정렬된 Store 목록
     */
    @Override
    public List<Store> findSimilarByEmbedding(Long storeId, List<Long> candidateStoreIds, int limit) {
        log.debug("[임베딩 유사도 정렬] Store ID: {}, 후보군: {}개, limit: {}",
                  storeId, candidateStoreIds.size(), limit);

        if (candidateStoreIds.isEmpty()) {
            return List.of();
        }

        String sql = """
            SELECT s.*
            FROM store s
            INNER JOIN store_embedding se ON s.id = se.store_id
            INNER JOIN store_embedding target_se ON target_se.store_id = :storeId
            WHERE s.id IN (:candidateIds)
              AND se.embedding IS NOT NULL
              AND target_se.embedding IS NOT NULL
              AND se.embedding_status = 'COMPLETED'
              AND target_se.embedding_status = 'COMPLETED'
            ORDER BY se.embedding <=> target_se.embedding
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql, Store.class);
        query.setParameter("storeId", storeId);
        query.setParameter("candidateIds", candidateStoreIds);
        query.setParameter("limit", limit);

        @SuppressWarnings("unchecked")
        List<Store> results = query.getResultList();

        log.debug("[임베딩 유사도 정렬] 최종 결과: {}개", results.size());
        return results;
    }
}
