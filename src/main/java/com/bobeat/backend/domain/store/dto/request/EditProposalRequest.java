package com.bobeat.backend.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

// TODO: 디자인 시안 안 나옴
@Schema(description = "정보 수정 제안 요청 DTO")
public record EditProposalRequest(
        @Schema(description = "제안 내용")
        String content
) {
}
