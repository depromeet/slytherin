package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
