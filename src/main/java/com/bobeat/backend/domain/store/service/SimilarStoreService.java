package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.store.dto.StoreWithDistance;
import com.bobeat.backend.domain.store.dto.response.SimilarStoreResponse;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import com.bobeat.backend.domain.store.repository.SimilarStoreRepository;
import com.bobeat.backend.domain.store.repository.StoreImageRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 유사 가게 추천 서비스
 *
 * 로직:
 * 1. PostGIS로 해당 가게로부터 3km 이내 가게 필터링
 * 2. 필터링된 후보군을 임베딩 벡터 유사도로 정렬
 * 3. 상위 5개 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarStoreService {

    private static final double MAX_DISTANCE_METERS = 3000.0; // 3km
    private static final int RESULT_LIMIT = 5;

    private final StoreRepository storeRepository;
    private final SimilarStoreRepository similarStoreRepository;
    private final StoreImageRepository storeImageRepository;

    /**
     * 유사 가게 추천(최대 5개)
     *
     * @param storeId       기준 가게 ID
     * @param userLatitude  유저의 현재 위도
     * @param userLongitude 유저의 현재 경도
     *
     * @return 유사 가게 목록
     */
    @Transactional(readOnly = true)
    public List<SimilarStoreResponse> findSimilarStores(Long storeId, Double userLatitude, Double userLongitude) {
        storeRepository.findByIdOrThrow(storeId);

        // 1단계: PostGIS로 3km 이내 가게 ID 추출
        List<Long> nearbyStoreIds = similarStoreRepository.findNearbyStoreIds(storeId, MAX_DISTANCE_METERS);

        if (nearbyStoreIds.isEmpty()) {
            return List.of();
        }

        // 2단계: 임베딩 벡터 유사도로 정렬하여 상위 5개 추출 (유저와의 거리 정보 포함)
        List<StoreWithDistance> similarStoresWithDistance = similarStoreRepository.findSimilarByEmbeddingWithDistance(
                storeId,
                nearbyStoreIds,
                RESULT_LIMIT,
                userLatitude,
                userLongitude
        );

        // N+1 쿼리 해결: StoreImage를 배치 조회
        List<Long> similarStoreIds = similarStoresWithDistance.stream()
                .map(storeWithDistance -> storeWithDistance.getStore().getId())
                .toList();

        Map<Long, StoreImage> storeImageMap = storeImageRepository.findMainImagesByStoreIds(similarStoreIds).stream()
                .collect(Collectors.toMap(img -> img.getStore().getId(), img -> img));

        return similarStoresWithDistance.stream()
                .map(storeWithDistance -> {
                    Store store = storeWithDistance.getStore();
                    Integer distance = storeWithDistance.getDistance();
                    StoreImage mainImage = storeImageMap.get(store.getId());
                    return SimilarStoreResponse.of(store, mainImage, distance);
                })
                .toList();
    }
}
