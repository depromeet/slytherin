package com.bobeat.backend.domain.store.dto.response;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.entity.SeatType;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

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

        @Schema(description = "혼밥 레벨 (1~5)")
        Integer honbobLevel,

        @Schema(description = "대분류 카테고리")
        String primaryCategory,

        @Schema(description = "현재 위치와의 거리 (미터)", example = "1500")
        Integer distanceInMeters,

        @Schema(description = "좌석 타입 목록")
        List<SeatType> seatTypes
) {
    /**
     * Store 엔티티, StoreImage, 거리 정보, 좌석 타입 리스트로부터 SimilarStoreResponse 생성
     */
    public static SimilarStoreResponse of(Store store, StoreImage storeImage, Integer distance, List<SeatType> seatTypes) {
        Level honbobLevel = store.getHonbobLevel();
        String primaryCategoryName = store.getCategories() != null
                && store.getCategories().getPrimaryCategory() != null
                ? store.getCategories().getPrimaryCategory().getPrimaryType()
                : null;

        return new SimilarStoreResponse(
                store.getId(),
                store.getName(),
                storeImage != null ? storeImage.getImageUrl() : null,
                honbobLevel != null ? honbobLevel.getValue() : null,
                primaryCategoryName,
                distance,
                seatTypes
        );
    }
}
