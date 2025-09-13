package com.bobeat.backend.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "가게 정보")
public class StoreInfo {
    
    @Schema(description = "가게 ID", example = "1")
    private Long id;

    @Schema(description = "가게명", example = "맛있는 식당")
    private String name;

    @Schema(description = "가게 이미지 URL")
    private String mainImageUrl;
}
