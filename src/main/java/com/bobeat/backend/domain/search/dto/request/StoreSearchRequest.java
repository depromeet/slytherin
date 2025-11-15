package com.bobeat.backend.domain.search.dto.request;

import com.bobeat.backend.global.request.CursorPaginationRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record StoreSearchRequest(

        @Schema(description = "검색어")
        @Valid @NotNull
        String query,

        @Schema(description = "페이징 정보")
        @Valid
        CursorPaginationRequest paging,

        @Schema(description = "위도", example = "37.5665")
        @NotNull
        Float lat,

        @Schema(description = "경도", example = "126.9780")
        @NotNull
        Float lon
) {
}
