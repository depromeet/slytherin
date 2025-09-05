package com.bobeat.backend.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "저장한 가게 정보 DTO")
public record SavedStoreDto(
        @Schema(description = "가게 ID")
        Long restaurantId,
        @Schema(description = "가게 이름")
        String name,
        @Schema(description = "가게 대표 이미지 URL")
        String thumbUrl,
        @Schema(description = "혼밥 레벨")
        int level
) {
}
