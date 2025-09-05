package com.bobeat.backend.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "저장한 가게 목록 조회 응답 DTO")
public record SavedStoreListResponse(
        @Schema(description = "저장한 가게 목록")
        List<SavedStoreDto> restaurants
) {
}
