package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.dto.StoreEmbeddingWithDistance;
import com.bobeat.backend.domain.store.dto.StoreEmbeddingWithDistanceDto;
import com.bobeat.backend.domain.store.entity.QStore;
import com.bobeat.backend.domain.store.entity.QStoreEmbedding;
import com.bobeat.backend.domain.store.entity.StoreEmbedding;
import com.bobeat.backend.domain.store.util.PgVectorUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreEmbeddingQueryRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

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

    public List<StoreEmbeddingWithDistanceDto> findSimilarEmbeddingsWithCursor(
            List<Float> queryEmbedding,
            Float lastScore,
            int limit,
            double userLon,
            double userLat
    ) {
        QStoreEmbedding se = QStoreEmbedding.storeEmbedding;
        QStore s = QStore.store;

        if (lastScore == null) {
            lastScore = 0.0f;
        }

        NumberExpression<Double> distanceExpr = Expressions.numberTemplate(
                Double.class,
                "ST_Distance(" +
                        "geography(ST_SetSRID(ST_MakePoint({0}, {1}), 4326)), " +
                        "geography(ST_SetSRID(ST_MakePoint({2}, {3}), 4326))" +
                        ")",
                userLon,
                userLat,
                s.address.latitude,
                s.address.longitude
        );

        NumberExpression<Double> similarityExpr = Expressions.numberTemplate(
                Double.class,
                "{0} <=> CAST({1} AS vector(1024))",
                se.embedding,
                PgVectorUtils.toLiteral(queryEmbedding)
        );

        return new JPAQuery<>(em)
                .select(Projections.constructor(
                        StoreEmbeddingWithDistance.class,
                        se,
                        distanceExpr
                ))
                .from(se)
                .join(s).on(se.store.id.eq(s.id))  // ManyToOne 관계 기준
                .where(similarityExpr.gt(lastScore))
                .orderBy(similarityExpr.asc(), s.id.asc())
                .limit(limit)
                .fetch();
    }
}