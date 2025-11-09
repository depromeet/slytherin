package com.bobeat.backend.domain.store.dto.response;

import com.bobeat.backend.domain.store.entity.SeatType;
import com.bobeat.backend.domain.store.external.kakao.dto.KakaoDocument;
import com.bobeat.backend.domain.store.external.kakao.dto.KakaoMenuDto;
import com.bobeat.backend.domain.store.external.kakao.dto.KakaoStoreDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
public record KakaoStoreResponse(
        @Schema(description = "가게 이름")
        String name,

        @Schema(description = "주소 정보")
        AddressResponse address,

        @Schema(description = "전화번호")
        String phoneNumber,

        @Schema(description = "가게 설명")
        String description,

        @Schema(description = "혼밥 레벨")
        Integer honbobLevel,

        @Schema(description = "메뉴 카테고리 정류 ex) 한식, 일식, 중식")
        CategoryResponse categories,

        @Schema(description = "가게 이미지 URL 목록")
        List<StoreImageResponse> storeImages,

        @Schema(description = "메뉴 목록")
        List<MenuResponse> menus,

        @Schema(description = "좌석 옵션 목록")
        List<SeatOptionResponse> seatOptions
) {
    @Builder
    @Schema(description = "주소 Dto")
    public record AddressResponse(
            @Schema(description = "주소")
            String address,

            @Schema(description = "위도")
            Double latitude,

            @Schema(description = "경도")
            Double longitude
    ) {
    }

    @Schema(description = "카테고리 요청 DTO")
    public record CategoryResponse(
            @Schema(description = "메뉴 카테고리 ID")
            String primaryCategory
    ) {
    }

    @Builder
    @Schema(description = "가게 이미지 DTO")
    public record StoreImageResponse(
            @Schema(description = "이미지 URL")
            String imageUrl,

            @Schema(description = "메인 이미지 여부")
            Boolean isMain
    ) {
    }

    @Builder
    @Schema(description = "메뉴 DTO")
    public record MenuResponse(
            @Schema(description = "메뉴 이름")
            String name,

            @Schema(description = "메뉴 가격")
            Long price,

            @Schema(description = "메뉴 이미지 URL")
            String imageUrl
    ) {
    }

    @Schema(description = "좌석 옵션 DTO")
    public record SeatOptionResponse(
            @Schema(description = "좌석 카테고리")
            SeatType seatType,

            @Schema(description = "좌석 이미지 URL")
            String imageUrl
    ) {
    }

    public static AddressResponse of(KakaoDocument kakaoDocument) {
        return AddressResponse.builder()
                .address(kakaoDocument.addressName())
                .longitude(Double.valueOf(kakaoDocument.x()))
                .latitude(Double.valueOf(kakaoDocument.y()))
                .build();

    }

    public static StoreImageResponse of(String imageUrls) {
        return StoreImageResponse.builder()
                .imageUrl(imageUrls)
                .isMain(false)
                .build();
    }

    public static MenuResponse of(KakaoMenuDto kakaoMenuDto) {
        return MenuResponse.builder()
                .name(kakaoMenuDto.name())
                .price(kakaoMenuDto.price())
                .imageUrl(kakaoMenuDto.imageUrl())
                .build();
    }

    public static KakaoStoreResponse of(KakaoDocument kakaoDocument, KakaoStoreDto kakaoStoreDto) {

        AddressResponse addressResponse = of(kakaoDocument);

        List<StoreImageResponse> storeImages = kakaoStoreDto.imageUrls().stream()
                .map(KakaoStoreResponse::of)
                .toList();
        List<MenuResponse> menuResponses = kakaoStoreDto.menuDtos().stream()
                .map(KakaoStoreResponse::of)
                .toList();

        return KakaoStoreResponse.builder()
                .name(kakaoStoreDto.name())
                .address(addressResponse)
                .phoneNumber(kakaoStoreDto.phoneNumber())
                .storeImages(storeImages)
                .menus(menuResponses)
                .build();
    }
}
