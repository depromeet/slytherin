package com.bobeat.backend.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 정보 DTO")
public record SeatInfoDto(
        @Schema(description = "전체 좌석 수")
        int total,
        @Schema(description = "1인석 수")
        int onePerson,
        @Schema(description = "바 좌석 수")
        int bar,
        @Schema(description = "2인석 수")
        int twoPerson
) {
}
