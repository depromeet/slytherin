package com.bobeat.backend.domain.review.dto.response;

import com.bobeat.backend.domain.review.entity.ReviewKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record MyReviewResponse(
        @Schema(description = "리뷰 ID", example = "1")
        Long id,

        @Schema(description = "가게 ID", example = "2")
        Long storeId,

        @Schema(description = "리뷰 내용", example = "맛있고 분위기도 좋아요!")
        String content,

        @Schema(description = "작성자 정보")
        ReviewerInfo reviewer,

        @Schema(description = "리뷰 키워드 목록")
        List<ReviewKeyword> keywords,

        @Schema(description = "작성일시")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        LocalDateTime updatedAt
) {
}
