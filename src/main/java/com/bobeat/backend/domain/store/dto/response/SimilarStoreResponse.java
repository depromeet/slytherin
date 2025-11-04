package com.bobeat.backend.domain.store.dto.response;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import com.bobeat.backend.domain.store.vo.Address;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 유사 가게 추천 응답 DTO
 */
@Schema(description = "유사 가게 추천 응답")
public record SimilarStoreResponse(
        @Schema(description = "가게 ID")
        Long id,

        @Schema(description = "가게 이름")
        String name,

        @Schema(description = "가게 대표 이미지 URL")
        String thumbnailUrl,

        @Schema(description = "주소")
        String address,

        @Schema(description = "위도", example = "37.58")
        Double latitude,

        @Schema(description = "경도", example = "127.0")
        Double longitude,

        @Schema(description = "혼밥 레벨 (1~5)")
        Integer honbobLevel
) {
    /**
     * Store 엔티티와 StoreImage로부터 SimilarStoreResponse 생성
     */
    public static SimilarStoreResponse of(Store store, StoreImage storeImage) {
        Address address = store.getAddress();
        Level honbobLevel = store.getHonbobLevel();

        return new SimilarStoreResponse(
                store.getId(),
                store.getName(),
                storeImage != null ? storeImage.getImageUrl() : null,
                address != null ? address.getAddress() : null,
                address != null ? address.getLatitude() : null,
                address != null ? address.getLongitude() : null,
                honbobLevel != null ? honbobLevel.getValue() : null
        );
    }
}
