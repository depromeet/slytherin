package com.bobeat.backend.domain.store.external.kakao.dto;

import java.util.List;

public record KakaoDocuments(
        List<KakaoDocument> documents
) {
}