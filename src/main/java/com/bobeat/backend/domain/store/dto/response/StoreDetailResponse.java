package com.bobeat.backend.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "가게 상세 정보 조회 응답 DTO")
public record StoreDetailResponse(
        @Schema(description = "가게 ID")
        Long restaurantId,
        @Schema(description = "가게 이름")
        String name,
        @Schema(description = "가게 대표 이미지 URL 목록")
        List<String> thumbnailUrls,
        @Schema(description = "혼밥 레벨")
        int level,
        @Schema(description = "카테고리")
        String category,
        @Schema(description = "주소")
        String address,
        @Schema(description = "전화번호")
        String phone,
        @Schema(description = "메뉴 목록")
        List<MenuDto> menus,
        @Schema(description = "좌석 정보")
        SeatInfoDto seatInfo,
        @Schema(description = "가격 비교 정보")
        PriceComparisonDto priceComparison,
        @Schema(description = "좌석 이미지 URL 목록")
        List<String> seatImages
) {
}
