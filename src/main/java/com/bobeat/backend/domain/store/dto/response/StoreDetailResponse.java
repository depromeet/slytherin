package com.bobeat.backend.domain.store.dto.response;

import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.SeatOption;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Schema(description = "가게 상세 정보 조회 응답 DTO")
@Builder
public record StoreDetailResponse(
        @Schema(description = "가게 ID")
        Long storeId,
        @Schema(description = "가게 대표 이미지 URL 목록")
        List<String> thumbnailUrls,
        @Schema(description = "혼밥 레벨")
        int level,
        @Schema(description = "가게 이름")
        String name,
        @Schema(description = "주소")
        String address,
        @Schema(description = "전화번호")
        String phone,
        @Schema(description = "메뉴 목록")
        List<MenuDto> menus,
        @Schema(description = "좌석 정보")
        SeatInfoDto seatInfo,
        @Schema(description = "좌석 이미지 URL 목록")
        List<SeatDto> seatImages
) {
    public static StoreDetailResponse of(Store store, List<StoreImage> storeImages, List<Menu> menus,
                                         List<SeatOption> seatOptions) {
        List<String> thumbnailUrls = storeImages.stream()
                .sorted((a, b) -> Boolean.compare(b.isMain(), a.isMain())) // true 먼저
                .map(StoreImage::getImageUrl) // String 리스트로 변환
                .toList();

        List<MenuDto> menuDtos = menus.stream()
                .map(MenuDto::from)
                .toList();

        SeatInfoDto seatInfoDto = SeatInfoDto.from(seatOptions);

        List<SeatDto> seatDtos = seatOptions.stream()
                .map(SeatDto::from)
                .toList();

        return StoreDetailResponse.builder()
                .storeId(store.getId())
                .thumbnailUrls(thumbnailUrls)
                .level(store.getHonbobLevel().getValue())
                .name(store.getName())
                .address(store.getAddress().getAddress())
                .phone(store.getPhoneNumber())
                .menus(menuDtos)
                .seatInfo(seatInfoDto)
                .seatImages(seatDtos)
                .build();
    }
}
