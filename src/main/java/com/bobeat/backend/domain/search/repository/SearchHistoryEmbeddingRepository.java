package com.bobeat.backend.domain.search.repository;

import com.bobeat.backend.domain.search.entity.SearchHistoryEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchHistoryEmbeddingRepository extends JpaRepository<SearchHistoryEmbedding, Long> {

    SearchHistoryEmbedding findByQuery(String query);
}
