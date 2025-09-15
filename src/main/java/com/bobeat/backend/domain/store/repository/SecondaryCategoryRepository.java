package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.SecondaryCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecondaryCategoryRepository extends JpaRepository<SecondaryCategory, Long> {
}