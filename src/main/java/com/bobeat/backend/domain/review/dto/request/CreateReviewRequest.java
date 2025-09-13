package com.bobeat.backend.domain.review.dto.request;

import com.bobeat.backend.domain.review.entity.ReviewKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Schema(description = "리뷰 작성 요청")
public class CreateReviewRequest {

    @NotNull(message = "가게 ID는 필수입니다")
    @Schema(description = "가게 ID", example = "1")
    private Long storeId;

    @NotBlank(message = "리뷰 내용은 필수입니다")
    @Schema(description = "리뷰 내용", example = "맛있고 분위기도 좋아요!")
    private String content;

    @Schema(description = "리뷰 키워드 목록")
    private List<ReviewKeyword> keywords;
}
