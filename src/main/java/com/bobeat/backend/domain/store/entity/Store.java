package com.bobeat.backend.domain.store.entity;

import com.bobeat.backend.domain.common.BaseTimeEntity;
import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.domain.store.vo.Categories;
import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store", indexes = {
    @Index(name = "idx_store_embedding_status", columnList = "embedding_status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Store extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    private Address address;

    private String phoneNumber;

    private String description;

    @Enumerated(EnumType.ORDINAL)
    private Level honbobLevel;

    @Embedded
    private Categories categories;

    /**
     * CLOVA AI 임베딩 벡터 (1024차원)
     * - 배치 작업이 비동기로 생성하여 저장합니다.
     * - 유사 가게 검색에 활용됩니다.
     */
    @Column(name = "embedding_vector", columnDefinition = "vector(1024)")
    private PGvector embeddingVector;

    /**
     * 수동 특징 벡터 (10차원)
     * - Store 생성/수정 시 즉시 계산되어 저장됩니다.
     * - 가게 추천에 활용됩니다.
     */
    @Column(name = "manual_vector", columnDefinition = "vector(10)")
    private PGvector manualVector;

    /**
     * 임베딩 벡터 생성 상태
     * - PENDING: 배치 작업에서 임베딩을 생성해야 함
     * - COMPLETED: 임베딩 생성 완료
     * - FAILED: 임베딩 생성 실패 (재시도 필요)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "embedding_status")
    private EmbeddingStatus embeddingStatus;

    /**
     * 임베딩 벡터를 업데이트합니다.
     */
    public void updateEmbeddingVector(PGvector embeddingVector) {
        this.embeddingVector = embeddingVector;
    }

    /**
     * 수동 특징 벡터를 업데이트합니다.
     */
    public void updateManualVector(PGvector manualVector) {
        this.manualVector = manualVector;
    }

    /**
     * 임베딩 상태를 변경합니다.
     */
    public void changeEmbeddingStatus(EmbeddingStatus embeddingStatus) {
        this.embeddingStatus = embeddingStatus;
    }

    /**
     * 임베딩 벡터 생성을 완료 처리합니다.
     */
    public void completeEmbedding(PGvector embeddingVector) {
        this.embeddingVector = embeddingVector;
        this.embeddingStatus = EmbeddingStatus.COMPLETED;
    }

    /**
     * 임베딩 벡터 생성 실패를 처리합니다.
     */
    public void failEmbedding() {
        this.embeddingStatus = EmbeddingStatus.FAILED;
    }
}
