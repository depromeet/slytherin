package com.bobeat.backend.domain.store.external.kakao.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record KakaoStoreDto(String name, String address, List<String> imageUrls, List<KakaoMenuDto> menuDtos) {
}
