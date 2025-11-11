package com.bobeat.backend.domain.store.external.kakao.dto;

import lombok.Builder;

@Builder
public record KakaoMenuDto(String imageUrl, String name, Long price) {
}
