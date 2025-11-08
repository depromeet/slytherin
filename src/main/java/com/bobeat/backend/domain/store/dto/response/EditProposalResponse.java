package com.bobeat.backend.domain.store.dto.response;

import com.bobeat.backend.domain.store.entity.ProposalType;
import com.bobeat.backend.domain.store.entity.StoreProposal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "정보 수정 제안 응답 DTO")
@Builder
public record EditProposalResponse(
        @Schema(description = "제안 id")
        Long id,
        @Schema(description = "제안 타입")
        ProposalType proposalType,
        @Schema(description = "제안 내용")
        String content
) {

    public static EditProposalResponse from(StoreProposal storeProposal) {
        return EditProposalResponse.builder()
                .id(storeProposal.getId())
                .proposalType(storeProposal.getProposalType())
                .content(storeProposal.getContent())
                .build();
    }
}
