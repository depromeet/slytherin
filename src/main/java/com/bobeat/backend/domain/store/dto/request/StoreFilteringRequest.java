package com.bobeat.backend.domain.store.dto.request;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.entity.SeatType;
import com.bobeat.backend.global.request.CursorPaginationRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record StoreFilteringRequest(
        @Schema(description = "위치기반 검색 bounding box")
        @Valid
        BoundingBox bbox,

        @Schema(description = "박스 중심점 위경도")
        @Valid @NotNull
        Coordinate center,

        @Schema(description = "필터 옵션")
        @Valid @NotNull
        Filters filters,

        @Schema(description = "페이징 정보")
        @Valid
        CursorPaginationRequest paging,

        @Schema(description = "정렬 기준 (DISTANCE: 거리순, RECOMMENDED: 추천순)", example = "DISTANCE")
        SortBy sortBy
) {
        public enum SortBy {
                DISTANCE,      // 거리순 (거리 100%)
                RECOMMENDED    // 추천순 (거리 70% + 내부점수 30%)
        }

        public record BoundingBox(
                @Schema(description = "박스 좌상단 위도", example = "37.5665")
                @NotNull
                Coordinate nw,

                @Schema(description = "박스 좌상단 경도", example = "126.9780")
                @NotNull
                Coordinate se
        ) {}

        public record Coordinate(
                @Schema(description = "위도", example = "37.5665")
                @NotNull
                Double lat,

                @Schema(description = "경도", example = "126.9780")
                @NotNull
                Double lon
        ) {}

        public record Filters(
                @Schema(description = "가격 범위")
                @Valid
                PriceRange price,

                @Schema(description = "혼밥 레벨 (1~4) - 단일 값 또는 배열 모두 지원", example = "[1, 2, 3]")
                @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                @NotNull
                List<Integer> honbobLevel,

                @Schema(description = "좌석 형태", example = "[\"FOR_ONE\", \"FOR_TWO\", \"FOR_FOUR\", \"CUBICLE\", \"BAR_TABLE\"]")
                List<SeatType> seatTypes,

                // TODO: ENUM 생성시 변경
                @Schema(description = "메뉴 카테고리 (한식, 일식, 중식, 양식, 패스트푸드, 분식, 아시아음식, 카페, 기타)", example = "[\"한식\", \"일식\"]")
                List<String> categories
        ) {}

        public record PriceRange(
                @Schema(description = "최소 가격(원)", example = "8000")
                Integer min,

                @Schema(description = "최대 가격(원)", example = "15000")
                Integer max
        ) {}
}
