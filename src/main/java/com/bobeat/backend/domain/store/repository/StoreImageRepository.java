package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {

    List<StoreImage> findByStore(Store store);

    void deleteByStore(Store store);
}
