package com.bobeat.backend.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가격 비교 정보 DTO")
public record PriceComparisonDto(
        @Schema(description = "대표 메뉴 가격(원)")
        int representativeMenuPrice,
        @Schema(description = "해당 지역 평균 점심값(원)")
        int averageLunchPrice,
        @Schema(description = "지역 평균 대비 대표 메뉴 가격 차이")
        int averagePriceDifference,
        @Schema(description = "지역 평균 대비 대표 메뉴 가격 정보")
        PriceComparisonAverage comparisonAverage
) {
}
