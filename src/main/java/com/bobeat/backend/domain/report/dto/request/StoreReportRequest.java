package com.bobeat.backend.domain.report.dto.request;

import com.bobeat.backend.domain.store.entity.SeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "식당 제보 등록 요청 DTO")
public record StoreReportRequest(
        @Schema(description = "가게 위치", example = "서울 강남구 테헤란로 1길")
        String location,
        @Schema(description = "가게 이름")
        String name,
        @Schema(description = "좌석 형태")
        List<SeatType> seatType,

        // TODO: ENUM 생성 후 ENUM으로 변경
        @Schema(description = "결제 방식", example = "[\"카운터 결제\"]")
        List<String> paymentMethods,

        // TODO: ENUM 생성 후 ENUM으로 변경
        @Schema(description = "메뉴 카테고리", example = "[\"한식\"]")
        List<String> menuCategories,

        @Schema(description = "추천 메뉴")
        String recommendedMenu,
        @Schema(description = "식당을 추천하는 이유")
        String reason,
        @Schema(description = "제보한 회원 ID (선택 사항)")
        Long memberId
) {
}
