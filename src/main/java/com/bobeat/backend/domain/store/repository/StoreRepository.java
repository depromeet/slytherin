package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {

    default Store findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
    }

    /**
     * 점수 업데이트가 필요한 식당들을 조회
     * (internalScore가 null이거나 scoreUpdateFlag가 true인 경우)
     */
    @Query("SELECT s FROM Store s WHERE s.internalScore IS NULL OR s.scoreUpdateFlag = true")
    List<Store> findStoresNeedingScoreUpdate();
}
