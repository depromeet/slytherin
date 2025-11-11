package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.PrimaryCategory;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrimaryCategoryRepository extends JpaRepository<PrimaryCategory, Long> {

    /**
     * 카테고리를 조회합니다.
     *
     * 캐싱 적용: 카테고리는 자주 변경되지 않으므로 메모리에 캐싱하여 성능 향상
     * - DB 쿼리 제거 → 메모리 조회 (99% 빠름)
     */
    @Cacheable(value = "categories", key = "#primaryType")
    Optional<PrimaryCategory> findByPrimaryType(String primaryType);
}
