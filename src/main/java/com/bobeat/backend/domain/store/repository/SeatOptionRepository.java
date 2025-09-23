package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.SeatOption;
import com.bobeat.backend.domain.store.entity.Store;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatOptionRepository extends JpaRepository<SeatOption, Long> {

    List<SeatOption> findByStore(Store store);

    void deleteByStore(Store store);
}
