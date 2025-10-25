package com.bobeat.backend.domain.store.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 임베딩 벡터 생성 상태
 * - PENDING: 임베딩 생성 대기 중
 * - COMPLETED: 임베딩 생성 완료
 * - FAILED: 임베딩 생성 실패 (재시도 가능)
 */
@AllArgsConstructor
@Getter
public enum EmbeddingStatus {
    PENDING("임베딩 생성 대기 중"),
    COMPLETED("임베딩 생성 완료"),
    FAILED("임베딩 생성 실패");

    private final String description;
}
