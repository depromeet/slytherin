package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.Store;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByStore(Store store);
    
    @Query("SELECT m FROM Menu m WHERE m.store IN :stores")
    List<Menu> findByStoreIn(@Param("stores") List<Store> stores);

    List<Menu> findTop3ByStoreAndRecommendFalseOrderByIdAsc(Store store);

    List<Menu> findTop3ByStoreAndRecommendTrueOrderByIdAsc(Store store);
}
