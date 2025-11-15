package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {

    default Store findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
    }

    /**
     * 점수 업데이트가 필요한 식당들을 조회 (internalScore가 null인 경우)
     */
    @Query("SELECT s FROM Store s WHERE s.internalScore IS NULL")
    List<Store> findStoresNeedingScoreUpdate();

    /**
     * 이름, 위도, 경도로 중복 가게 확인
     */
    boolean existsByNameAndAddress_LatitudeAndAddress_Longitude(String name, Double latitude, Double longitude);

    Map<Long, Integer> findDistance(List<Long> storeIds, float userLat, float userLon);
}
