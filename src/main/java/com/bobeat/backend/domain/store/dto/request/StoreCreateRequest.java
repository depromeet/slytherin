package com.bobeat.backend.domain.store.dto.request;

import com.bobeat.backend.domain.store.entity.SeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(description = "어드민 가게 등록 요청 DTO")
public record StoreCreateRequest(
        @Schema(description = "가게 이름")
        @NotBlank(message = "가게 이름은 필수입니다")
        String name,

        @Schema(description = "주소 정보")
        @NotNull(message = "주소 정보는 필수입니다")
        @Valid
        AddressRequest address,

        @Schema(description = "전화번호")
        String phoneNumber,

        @Schema(description = "가게 설명")
        String description,

        @Schema(description = "메인 이미지 URL")
        String mainImageUrl,

        @Schema(description = "혼밥 레벨")
        @NotNull(message = "혼밥 레벨은 필수입니다")
        @Positive(message = "혼밥 레벨은 1 이상이어야 합니다")
        Integer honbobLevel,

        @Schema(description = "메뉴 카테고리 정류 ex) 한식, 일식, 중식")
        @NotNull(message = "메뉴 카테고리는 필수입니다")
        @Valid
        CategoryRequest categories,

        @Schema(description = "가게 이미지 URL 목록")
        @NotEmpty(message = "가게 이미지는 최소 1개 이상 필요합니다")
        List<StoreImageRequest> storeImages,

        @Schema(description = "메뉴 목록")
        @NotEmpty(message = "메뉴는 최소 1개 이상 필요합니다")
        List<MenuRequest> menus,

        @Schema(description = "좌석 옵션 목록")
        @NotEmpty(message = "좌석 옵션은 최소 1개 이상 필요합니다")
        List<SeatOptionRequest> seatOptions
) {

    @Schema(description = "주소 요청 DTO")
    public record AddressRequest(
            @Schema(description = "주소")
            @NotBlank(message = "주소는 필수입니다")
            String address,

            @Schema(description = "위도")
            @NotNull(message = "위도는 필수입니다")
            Double latitude,

            @Schema(description = "경도")
            @NotNull(message = "경도는 필수입니다")
            Double longitude
    ) {}

    @Schema(description = "카테고리 요청 DTO")
    public record CategoryRequest(
            @Schema(description = "메뉴 카테고리 ID")
            @NotNull(message = "메뉴 카테고리는 필수입니다")
            String primaryCategory
    ) {}

    @Schema(description = "가게 이미지 요청 DTO")
    public record StoreImageRequest(
            @Schema(description = "이미지 URL")
            @NotBlank(message = "이미지 URL은 필수입니다")
            String imageUrl,

            @Schema(description = "메인 이미지 여부")
            @NotNull(message = "메인 이미지 여부는 필수입니다")
            Boolean isMain
    ) {}

    @Schema(description = "메뉴 요청 DTO")
    public record MenuRequest(
            @Schema(description = "메뉴 이름")
            @NotBlank(message = "메뉴 이름은 필수입니다")
            String name,

            @Schema(description = "메뉴 가격")
            @NotNull(message = "메뉴 가격은 필수입니다")
            @Positive(message = "메뉴 가격은 0보다 커야 합니다")
            Integer price,

            @Schema(description = "메뉴 이미지 URL")
            String imageUrl
    ) {}

    @Schema(description = "좌석 옵션 요청 DTO")
    public record SeatOptionRequest(
            @Schema(description = "좌석 카테고리")
            @NotBlank(message = "좌석 카테고리는 필수입니다")
            SeatType seatType,

            @Schema(description = "좌석 이미지 URL")
            String imageUrl
    ) {}
}
