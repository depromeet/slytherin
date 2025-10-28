package com.bobeat.backend.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "검색 히스토리 DTO")
@Builder
public record SearchHistoryDto(
        @Schema(description = "좌석 이미지")
        String name,
        @Schema(description = "좌석 타입")
        String seatType) {
}
