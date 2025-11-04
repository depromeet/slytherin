package com.bobeat.backend.domain.store.external.clova.service;

import com.bobeat.backend.domain.store.external.clova.dto.ClovaEmbeddingRequest;
import com.bobeat.backend.domain.store.external.clova.dto.ClovaEmbeddingResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClovaEmbeddingClient {

    private final WebClient clovaWebClient;

    @Value("${ncp.clova.embed-path}")
    private String embeddingApiUrl;


    public Mono<List<Float>> getEmbedding(String text) {
        ClovaEmbeddingRequest requestBody = new ClovaEmbeddingRequest(text);
        String requestId = UUID.randomUUID().toString();

        return clovaWebClient.post()
                .uri(embeddingApiUrl)
                .header("X-NCP-CLOVASTUDIO-REQUEST-ID", requestId)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    // ⭐️ System.err 대신 로거 사용
                                    log.error("CLOVA API Error (Request ID: {}): {}", requestId, errorBody);
                                    return Mono.error(new RuntimeException("CLOVA API 요청 실패: " + errorBody));
                                })
                )
                .bodyToMono(ClovaEmbeddingResponse.class)
                .map(response -> response.getResult().getEmbedding())
                .timeout(Duration.ofSeconds(10));
    }

    public List<Float> getEmbeddingSync(String text) {
        return getEmbedding(text).block();
    }
}
