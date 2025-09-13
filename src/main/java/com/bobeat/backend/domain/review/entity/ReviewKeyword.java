package com.bobeat.backend.domain.review.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewKeyword {
    
    BEST_TASTE("맛은 최고"),
    GOOD_FOR_SOLO("혼밥 대박"),
    KIND_SERVICE("친절한 응대"),
    NICE_STAFF("재밌던 직원"),
    OPEN_ATMOSPHERE("보통의 맛");
    
    private final String displayName;
}
