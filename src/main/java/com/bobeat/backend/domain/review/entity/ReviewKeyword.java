package com.bobeat.backend.domain.review.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewKeyword {

    FAST_TURNOVER("빠른 회전율"),
    SPACIOUS_STORE("넓은 매장"),
    KIND_SERVICE("친절한 응대"),
    AFFORDABLE_MEAL("저렴한 한끼"),
    GUARANTEED_TASTE("보증된 맛");

    private final String displayName;
}
