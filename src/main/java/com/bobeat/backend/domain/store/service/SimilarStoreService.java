package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.store.dto.response.SimilarStoreResponse;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import com.bobeat.backend.domain.store.repository.SimilarStoreRepository;
import com.bobeat.backend.domain.store.repository.StoreImageRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import java.util.List;
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
     */
    @Transactional(readOnly = true)
    public List<SimilarStoreResponse> findSimilarStores(Long storeId) {
        storeRepository.findByIdOrThrow(storeId);

        // 1단계: PostGIS로 3km 이내 가게 ID 추출
        List<Long> nearbyStoreIds = similarStoreRepository.findNearbyStoreIds(storeId, MAX_DISTANCE_METERS);

        if (nearbyStoreIds.isEmpty()) {
            return List.of();
        }

        // 2단계: 임베딩 벡터 유사도로 정렬하여 상위 5개 추출
        List<Store> similarStores = similarStoreRepository.findSimilarByEmbedding(
                storeId,
                nearbyStoreIds,
                RESULT_LIMIT
        );

        return similarStores.stream()
                .map(store -> {
                    StoreImage mainImage = storeImageRepository.findByStoreAndIsMainTrue(store);
                    return SimilarStoreResponse.of(store, mainImage);
                })
                .toList();
    }
}
