package com.bobeat.backend.global.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PagingRequest(
        @Schema(description = "페이지 번호(0부터 시작)", example = "0")
        @NotNull
        Integer page,

        @Schema(description = "페이지 크기", example = "20")
        Integer size
) { }
