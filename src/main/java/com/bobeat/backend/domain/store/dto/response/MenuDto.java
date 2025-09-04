package com.bobeat.backend.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 정보 DTO")
public record MenuDto(
        @Schema(description = "메뉴 이름")
        String name,
        @Schema(description = "메뉴 가격")
        int price,
        @Schema(description = "대표 메뉴 여부")
        boolean isRepresentative
) {
}
