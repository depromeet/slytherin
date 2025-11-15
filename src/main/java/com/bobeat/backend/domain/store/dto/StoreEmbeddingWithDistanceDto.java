package com.bobeat.backend.domain.store.dto;

import com.bobeat.backend.domain.store.entity.StoreEmbedding;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoreEmbeddingWithDistanceDto {
    private final StoreEmbedding embedding;
    private final Integer distance;
}