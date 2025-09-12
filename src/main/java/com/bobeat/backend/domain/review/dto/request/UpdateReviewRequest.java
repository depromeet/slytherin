package com.bobeat.backend.domain.review.dto.request;

import com.bobeat.backend.domain.review.entity.ReviewKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "리뷰 수정 요청")
public class UpdateReviewRequest {

    @NotBlank(message = "리뷰 내용은 필수입니다")
    @Schema(description = "리뷰 내용", example = "맛있고 분위기도 좋아요!")
    private String content;

    @Schema(description = "리뷰 키워드 목록")
    private List<ReviewKeyword> keywords;
}
