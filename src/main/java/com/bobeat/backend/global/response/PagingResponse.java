package com.bobeat.backend.global.response;


import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "페이징 응답 DTO")
public record PagingResponse<T> (

    @Schema(description = "조회된 데이터 리스트")
    List<T> contents,

    @Schema(description = "현재 페이지 번호")
    int pageNumber,

    @Schema(description = "페이지 크기")
    int pageSize,

    @Schema(description = "전체 항목 수")
    int totalElements,

    @Schema(description = "전체 페이지 수")
    int totalPages,

    @Schema(description = "마지막 페이지 여부")
    boolean isLast
){
    public static <T> PagingResponse<T> of(List<T> content, int pageNumber, int pageSize, int totalElements, int totalPages, boolean isLast) {
        return new PagingResponse<>(content, pageNumber, pageSize, totalElements, totalPages, isLast);
    }
}