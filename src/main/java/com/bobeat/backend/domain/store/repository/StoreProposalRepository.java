package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.StoreProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreProposalRepository extends JpaRepository<StoreProposal, Long> {

    void deleteByStoreId(Long storeId);
}
