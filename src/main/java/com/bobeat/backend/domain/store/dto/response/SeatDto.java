package com.bobeat.backend.domain.store.dto.response;

import com.bobeat.backend.domain.store.entity.SeatOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "좌석 이미지 DTO")
@Builder
public record SeatDto(
        @Schema(description = "좌석 이미지")
        String imageUrl,
        @Schema(description = "좌석 타입")
        String seatType
) {
    public static SeatDto from(SeatOption seatOption) {
        return SeatDto.builder()
                .imageUrl(seatOption.getImageUrl())
                .seatType(seatOption.getSeatType().name())
                .build();
    }
}
