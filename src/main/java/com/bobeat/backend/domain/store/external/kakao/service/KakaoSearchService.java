package com.bobeat.backend.domain.store.external.kakao.service;

import com.bobeat.backend.domain.store.external.kakao.dto.KakaoDocument;
import com.fasterxml.jackson.databind.JsonNode;
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
        System.out.println("kakaoApiKey = " + kakaoApiKey);
        JsonNode response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", query)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoApiKey)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response != null && response.has("documents")) {
            JsonNode documents = response.get("documents");
            if (documents.isArray() && documents.size() > 0) {
                JsonNode first = documents.get(0);
                return KakaoDocument.builder()
                        .addressName(first.get("address_name").asText())
                        .categoryGroupCode(first.get("category_group_code").asText())
                        .categoryGroupName(first.get("category_group_name").asText())
                        .categoryName(first.get("category_name").asText())
                        .distance(first.get("distance").asText())
                        .id((first.get("id")).asText())
                        .phone(first.get("phone").asText())
                        .placeName(first.get("place_name").asText())
                        .placeUrl(first.get("place_url").asText())
                        .roadAddressName(first.get("road_address_name").asText())
                        .x(first.get("x").asText())
                        .y(first.get("y").asText())
                        .build();
            }
        }

        throw new RuntimeException("찾을 수 없습니다");
    }

}