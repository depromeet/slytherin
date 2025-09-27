package com.bobeat.backend.domain.member.entity;

import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Level {
    LEVEL_0(0, "미정"),        // ordinal=0, DB에는 없지만 정렬 맞추기용
    LEVEL_1(1, "혼밥 입문자"),  // ordinal=1, DB value=1
    LEVEL_2(2, "혼밥 탐험가"),  // ordinal=2, DB value=2
    LEVEL_3(3, "혼밥 숙련자"),  // ordinal=3, DB value=3
    LEVEL_4(4, "혼밥 고수");   // ordinal=4, DB value=4

    private final int value;
    private final String description;

    public static Level fromValue(int value) {
        for (Level level : Level.values()) {
            if (level.getValue() == value) {
                return level;
            }
        }
        throw new CustomException(ErrorCode.INVALID_LEVEL_VALUE);
    }
}
