package com.bobeat.backend.domain.onboarding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "온보딩 결과 등록 요청 DTO")
public record OnboardingRequest(
        @Schema(description = "질문 답변 목록")
        List<OnboardingAnswerDto> answers
) {
}
