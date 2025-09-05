package com.bobeat.backend.domain.store.dto.response;

import lombok.Getter;

@Getter
public enum PriceComparisonAverage {
    MUCH_MORE_EXPENSIVE("훨씬 비쌈"),
    MORE_EXPENSIVE("비쌈"),
    SIMILAR("평균과 비슷함"),
    CHEAPER("쌈"),
    MUCH_CHEAPER("훨씬 쌈");

    private final String description;

    PriceComparisonAverage(String description) {
        this.description = description;
    }
}
