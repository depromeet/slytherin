package com.bobeat.backend.domain.store.dto.response;

import com.bobeat.backend.domain.store.entity.SeatOption;
import com.bobeat.backend.domain.store.entity.SeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Schema(description = "좌석 정보 DTO")
@Builder
public record SeatInfoDto(
        @Schema(description = "칸막이 좌석 유무")
        boolean cubicle,
        @Schema(description = "바 좌석 수")
        boolean barTable,
        @Schema(description = "1인석 수")
        boolean forOne,
        @Schema(description = "2인석 수")
        boolean forTwo,
        @Schema(description = "4인석 수")
        boolean forFour
) {
    public static SeatInfoDto from(List<SeatOption> seatOptions) {
        boolean hasForOne = seatOptions.stream()
                .anyMatch(option -> option.getSeatType() == SeatType.FOR_ONE);

        boolean hasForTwo = seatOptions.stream()
                .anyMatch(option -> option.getSeatType() == SeatType.FOR_TWO);

        boolean hasForFour = seatOptions.stream()
                .anyMatch(option -> option.getSeatType() == SeatType.FOR_FOUR);

        boolean hasBarTable = seatOptions.stream()
                .anyMatch(option -> option.getSeatType() == SeatType.BAR_TABLE);

        boolean hasCubicle = seatOptions.stream()
                .anyMatch(option -> option.getSeatType() == SeatType.CUBICLE);

        return SeatInfoDto.builder()
                .forOne(hasForOne)
                .forTwo(hasForTwo)
                .forFour(hasForFour)
                .barTable(hasBarTable)
                .cubicle(hasCubicle)
                .build();
    }
}
