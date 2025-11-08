package com.bobeat.backend.domain.store.dto;

import com.bobeat.backend.domain.store.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Store와 거리 정보를 함께 담는 DTO
 */
@Getter
@AllArgsConstructor
public class StoreWithDistance {
    private Store store;
    private Integer distance;
}
