package com.bobeat.backend.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "위치 기반 가게 검색 응답 DTO")
public record StoreSearchResponse(
        @Schema(description = "가게 목록")
        List<StoreSearchResultDto> items,
        @Schema(description = "다음 페이지 조회용 커서")
        String nextCursor
) {
}
