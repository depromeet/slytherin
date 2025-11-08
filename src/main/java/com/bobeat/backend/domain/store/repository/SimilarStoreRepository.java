package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.dto.StoreWithDistance;
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
     * 후보 가게 ID 목록 중에서 기준 가게와 임베딩 유사도가 높은 순으로 정렬하여 반환 (유저와의 거리 정보 포함)
     *
     * @param storeId 기준 가게 ID
     * @param candidateStoreIds 후보 가게 ID 목록
     * @param limit 반환할 최대 개수
     * @param userLatitude 유저의 현재 위도
     * @param userLongitude 유저의 현재 경도
     * @return 임베딩 유사도 순으로 정렬된 Store와 유저와의 거리 정보 목록
     */
    List<StoreWithDistance> findSimilarByEmbeddingWithDistance(Long storeId, List<Long> candidateStoreIds, int limit, Double userLatitude, Double userLongitude);
}
