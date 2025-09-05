package com.bobeat.backend.domain.store.dto.request;

import com.bobeat.backend.domain.store.entity.SeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record StoreFilteringRequest(
        @Schema(description = "박스 좌상단 위도", example = "37.58")
        @NotNull
        Double nwLat,

        @Schema(description = "박스 좌상단 경도", example = "127.0")
        @NotNull
        Double nwLon,

        @Schema(description = "박스 우하단 위도", example = "37.55")
        @NotNull
        Double seLat,

        @Schema(description = "박스 우하단 경도", example = "127.05")
        @NotNull
        Double seLon,

        @Schema(description = "박스 중심점 위도", example = "37.56")
        @NotNull
        Double centerLat,

        @Schema(description = "박스 중심점 경도", example = "127.02")
        @NotNull
        Double centerLon,

        @Schema(description = "최소 메뉴 가격", example = "10000")
        Integer priceMin,

        @Schema(description = "최대 메뉴 가격", example = "30000")
        Integer priceMax,

        @Schema(description = "혼밥 레벨 (1~5)", example = "3")
        @NotNull
        Integer level,

        @Schema(description = "좌석 형태", example = "[\"FOR_ONE\", \"BAR\"]")
        List<SeatType> seatType,

        // TODO: ENUM 생성시 변경
        @Schema(description = "결제 방식", example = "[\"CARD\", \"ZERO_PAY\"]")
        List<String> paymentMethods,

        // TODO: ENUM 생성시 변경
        @Schema(description = "메뉴 카테고리", example = "[\"korean\", \"japanese\"]")
        List<String> menuCategories
) {
}
