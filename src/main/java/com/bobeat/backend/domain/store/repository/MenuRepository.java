package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.Store;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByStore(Store store);

    void deleteByStore(Store store);
}
