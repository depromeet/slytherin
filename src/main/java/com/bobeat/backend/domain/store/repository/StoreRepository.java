package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.EmbeddingStatus;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {

    default Store findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
    }

    /**
     * 임베딩 상태가 PENDING 또는 FAILED인 Store를 조회합니다.
     * 배치 작업에서 임베딩을 생성해야 할 대상을 찾는 데 사용됩니다.
     *
     * @param statuses 조회할 상태 목록 (PENDING, FAILED)
     * @param pageable 페이징 정보
     * @return 해당 상태의 Store 목록
     */
    Page<Store> findByEmbeddingStatusIn(List<EmbeddingStatus> statuses, Pageable pageable);
}
