package com.bobeat.backend.domain.onboarding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "온보딩 질문 답변 DTO")
public record OnboardingAnswerDto(
        @Schema(description = "질문 순서 (1~5)")
        int questionOrder,
        @Schema(description = "선택한 옵션 순번 (1부터 시작)")
        int selectedOption
) {
}
