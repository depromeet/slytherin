package com.bobeat.backend.domain.store.dto.request;

import com.bobeat.backend.domain.store.entity.ProposalType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "정보 수정 제안 요청 DTO")
@Builder
public record EditProposalRequest(

        @Schema(description = "제안 타입")
        ProposalType proposalType,
        @Schema(description = "제안 내용", maximum = "120")
        String content
) {
}
