package com.bobeat.backend.domain.store.dto.response;

import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "위치 기반 가게 검색 response")
public record StoreSearchResultDto(
        @Schema(description = "가게 ID")
        Long id,

        @Schema(description = "가게 이름")
        String name,

        @Schema(description = "가게 대표 이미지 URL")
        String thumbnailUrl,

        @Schema(description = "대표 메뉴")
        SignatureMenu signatureMenu,

        @Schema(description = "위/경도 정보")
        Coordinate coordinate,

        @Schema(description = "현재 위치로부터 거리(m)")
        int distance,

        @Schema(description = "도보 소요 시간")
        int walkingMinutes,

        @Schema(description = "좌석 형태")
        List<String> seatTypes,

        @Schema(description = "태그")
        List<String> tags,

        @Schema(description = "혼밥레벨")
        int honbobLevel
) {
    @Schema(description = "대표 메뉴정보")
    public record SignatureMenu(
            @Schema(description = "대표 메뉴 이름")
            String name,

            @Schema(description = "대표 메뉴 가격(원)")
            int price
    ) {
    }

    @Schema(description = "위경도 정보")
    public record Coordinate(
            @Schema(description = "위도", example = "37.58")
            @NotNull
            Double lat,

            @Schema(description = "경도", example = "127.0")
            @NotNull
            Double lon
    ) {
    }

    public static StoreSearchResultDto of(Store store, StoreImage storeImage, List<String> seatTypeNames,
                                          List<String> tagNames) {
        return new StoreSearchResultDto(store.getId(),
                store.getName(),
                storeImage.getImageUrl(),
                new StoreSearchResultDto.SignatureMenu(null, 0),
                new Coordinate(store.getAddress().getLatitude(), store.getAddress().getLongitude()),
                10,
                10,
                seatTypeNames,
                tagNames,
                store.getHonbobLevel().getValue());
    }
}
