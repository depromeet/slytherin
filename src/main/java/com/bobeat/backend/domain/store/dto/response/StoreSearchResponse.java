package com.bobeat.backend.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "위치 기반 가게 검색 response")
public record StoreSearchResponse(
        @Schema(description = "가게 ID")
        Long id,

        @Schema(description = "가게 이름")
        String name,

        @Schema(description = "가게 대표 이미지 URL")
        String thumbnailUrl,

        @Schema(description = "대표 메뉴")
        SignatureMenu signatureMenu,

        @Schema(description = "현재 위치로부터 거리(m)")
        int distance,

        @Schema(description = "좌석 형태")
        List<String> seats,

        @Schema(description = "메뉴 카테고리")
        List<String> categories,

        @Schema(description = "사용자가 저장한 가게 여부")
        boolean saved
) {
        @Schema(description = "대표 메뉴정보")
        public record SignatureMenu(
                @Schema(description = "대표 메뉴 이름")
                String name,

                @Schema(description = "대표 메뉴 가격(원)")
                int price
        ) {}
}
