package com.bobeat.backend.domain.store.external.kakao.service;

import static com.bobeat.backend.global.exception.ErrorCode.NOT_FOUND_STORE;

import com.bobeat.backend.domain.store.external.kakao.dto.KakaoDocument;
import com.bobeat.backend.domain.store.external.kakao.dto.KakaoDocuments;
import com.bobeat.backend.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KakaoSearchService {

    private WebClient webClient;

    @Value("${crawler.kakao.api-key}")
    private String kakaoApiKey;

    public KakaoSearchService() {
        webClient = WebClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .build();
    }

    public KakaoDocument searchAddress(String query) {
        KakaoDocuments response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", query)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoApiKey)
                .retrieve()
                .bodyToMono(com.bobeat.backend.domain.store.external.kakao.dto.KakaoDocuments.class)
                .block();

        if (response != null && response.documents() != null && !response.documents().isEmpty()) {
            return response.documents().get(0);
        }
        throw new CustomException(NOT_FOUND_STORE);
    }

}