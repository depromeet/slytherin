package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.PrimaryCategory;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrimaryCategoryRepository extends JpaRepository<PrimaryCategory, Long> {
    Optional<PrimaryCategory> findByPrimaryType(String primaryType);
}
