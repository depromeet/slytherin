package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreEmbedding;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreEmbeddingRepository extends JpaRepository<StoreEmbedding, Long> {

    Optional<StoreEmbedding> findByStore(Store store);

    @Query(value = """
            SELECT store_embedding.* FROM store_embedding se
            WHERE (:lastDistance IS NULL
                   OR (store_embedding.embedding <=> CAST(:embedding AS vector(1024))) > :lastDistance
                   OR ((store_embedding.embedding <=> CAST(:embedding AS vector(1024))) = :lastDistance
                                   AND store_embedding.id > :lastId))
            ORDER BY store_embedding.embedding <=> CAST(:embedding AS vector(1024)) ASC, se.id ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<StoreEmbedding> findSimilarEmbeddingsWithCursor(
            @Param("embedding") String embedding,
            @Param("lastDistance") Double lastDistance,
            @Param("lastId") Long lastId,
            @Param("limit") int limit
    );

    void deleteByStoreId(Long storeId);
}
