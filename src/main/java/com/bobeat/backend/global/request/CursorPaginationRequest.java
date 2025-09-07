package com.bobeat.backend.global.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CursorPaginationRequest(
        @Schema(description = "한 번에 조회할 최대 항목 수")
        @NotNull
        int limit,
        @Schema(description = "지난 응답 때 마지막 커서ID")
        String lastKnown
) {
}
