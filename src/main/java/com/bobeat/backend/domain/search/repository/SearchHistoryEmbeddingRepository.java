package com.bobeat.backend.domain.search.repository;

import com.bobeat.backend.domain.search.entity.SearchHistoryEmbedding;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchHistoryEmbeddingRepository extends JpaRepository<SearchHistoryEmbedding, Long> {


    @Cacheable(value = "searchEmbeddings", key = "#query", unless = "#result == null")
    SearchHistoryEmbedding findByQuery(String query);

    @CachePut(value = "searchEmbeddings", key = "#entity.query")
    default SearchHistoryEmbedding saveAndCache(SearchHistoryEmbedding entity) {
        return save(entity);
    }
}
