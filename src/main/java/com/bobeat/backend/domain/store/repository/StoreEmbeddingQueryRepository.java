package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.entity.StoreEmbedding;
import com.bobeat.backend.domain.store.util.PgVectorUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreEmbeddingQueryRepository {

    private final EntityManager em;

    public List<StoreEmbedding> findSimilarEmbeddingsWithCursor(
            List<Float> embedding,
            Float lastScore,
            int limit
    ) {
        String vectorLiteral = PgVectorUtils.toLiteral(embedding);
        if (lastScore == null) {
            lastScore = Float.valueOf(0.0f);
        }
        String sql = """
                    SELECT 
                        se.*
                    FROM store_embedding se
                    JOIN store s ON s.id = se.store_id
                    WHERE (se.embedding <=> CAST(:embedding AS vector(1024))) > :lastScore
                    ORDER BY (se.embedding <=> CAST(:embedding AS vector(1024))) ASC, s.id ASC
                    LIMIT :limit
                """;

        Query query = em.createNativeQuery(sql, StoreEmbedding.class);
        query.setParameter("embedding", vectorLiteral);
        query.setParameter("limit", limit);
        query.setParameter("lastScore", lastScore);
        return query.getResultList();
    }
}