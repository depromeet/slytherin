package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {

    List<StoreImage> findByStore(Store store);

    StoreImage findByStoreAndIsMainTrue(Store store);

    
    @Query("""
        SELECT si FROM StoreImage si
        WHERE si.store.id IN :storeIds AND si.isMain = true
        """)
    List<StoreImage> findMainImagesByStoreIds(@Param("storeIds") List<Long> storeIds);

    void deleteByStoreId(Long storeId);
}
