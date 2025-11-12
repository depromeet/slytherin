package com.bobeat.backend.domain.review.dto.response;

import com.bobeat.backend.domain.review.entity.Review;
import com.bobeat.backend.domain.review.entity.ReviewKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "리뷰 응답")
public class ReviewResponse {

    @Schema(description = "리뷰 ID", example = "1")
    private Long id;

    @Schema(description = "가게 ID", example = "3")
    private Long storeId;

    @Schema(description = "리뷰 내용", example = "맛있고 분위기도 좋아요!")
    private String content;

    @Schema(description = "작성자 정보")
    private ReviewerInfo reviewer;

    @Schema(description = "리뷰 키워드 목록")
    private List<ReviewKeyword> keywords;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getStore().getId(),
                review.getContent(),
                new ReviewerInfo(
                        review.getMember().getId(),
                        review.getMember().getNickname(),
                        review.getMember().getProfileImageUrl(),
                        review.getMember().getOnboardingProfile().getHonbobLevel().getValue()
                ),
                review.getKeywords(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}