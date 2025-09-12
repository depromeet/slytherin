package com.bobeat.backend.domain.store.dto.response;

import com.bobeat.backend.domain.store.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "메뉴 정보 DTO")
@Builder
public record MenuDto(
        @Schema(description = "메뉴 이름")
        String name,
        @Schema(description = "메뉴 가격")
        int price,
        @Schema(description = "대표 메뉴 여부")
        boolean isRepresentative
) {
    public static MenuDto from(Menu menu) {
        return MenuDto.builder()
                .name(menu.getName())
                .price(menu.getPrice())
                .isRepresentative(menu.isRecommend())
                .build();
    }
}
