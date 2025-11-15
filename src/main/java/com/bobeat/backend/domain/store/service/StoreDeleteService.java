package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.review.repository.ReviewRepository;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.repository.MenuRepository;
import com.bobeat.backend.domain.store.repository.SeatOptionRepository;
import com.bobeat.backend.domain.store.repository.StoreEmbeddingRepository;
import com.bobeat.backend.domain.store.repository.StoreImageRepository;
import com.bobeat.backend.domain.store.repository.StoreProposalRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class StoreDeleteService {

    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;
    private final MenuRepository menuRepository;
    private final SeatOptionRepository seatOptionRepository;
    private final StoreEmbeddingRepository storeEmbeddingRepository;
    private final ReviewRepository reviewRepository;
    private final StoreProposalRepository storeProposalRepository;

    @Transactional
    public void deleteStore(Long storeId) {
        Store store = storeRepository.findByIdOrThrow(storeId);
        deleteRelatedEntities(storeId);
        storeRepository.delete(store);
    }

    private void deleteRelatedEntities(Long storeId) {
        storeImageRepository.deleteByStoreId(storeId);
        menuRepository.deleteByStoreId(storeId);
        seatOptionRepository.deleteByStoreId(storeId);
        storeEmbeddingRepository.deleteByStoreId(storeId);
        reviewRepository.deleteByStoreId(storeId);
        storeProposalRepository.deleteByStoreId(storeId);
        // Note: MemberSavedStore는 Repository가 없어서 제외
        // 필요시 추가 구현 필요
    }
}
