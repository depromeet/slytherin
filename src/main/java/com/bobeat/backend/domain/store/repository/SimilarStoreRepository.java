package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.Store;

import java.util.List;

/**
 * 유사 가게 추천을 위한 Repository 인터페이스
 */
public interface SimilarStoreRepository {

    /**
     * 특정 가게로부터 지정된 거리 이내에 있는 가게 ID 목록을 반환 (PostGIS 사용)
     *
     * @param storeId 기준 가게 ID
     * @param maxDistanceMeters 최대 거리 (미터 단위)
     * @return 거리 이내에 있는 가게 ID 목록
     */
    List<Long> findNearbyStoreIds(Long storeId, double maxDistanceMeters);

    /**
     * 후보 가게 ID 목록 중에서 기준 가게와 임베딩 유사도가 높은 순으로 정렬하여 반환
     *
     * @param storeId 기준 가게 ID
     * @param candidateStoreIds 후보 가게 ID 목록
     * @param limit 반환할 최대 개수
     * @return 임베딩 유사도 순으로 정렬된 Store 목록
     */
    List<Store> findSimilarByEmbedding(Long storeId, List<Long> candidateStoreIds, int limit);
}
