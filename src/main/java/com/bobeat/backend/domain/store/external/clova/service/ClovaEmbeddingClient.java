package com.bobeat.backend.domain.store.external.clova.service;

import com.bobeat.backend.domain.store.external.clova.dto.ClovaEmbeddingRequest;
import com.bobeat.backend.domain.store.external.clova.dto.ClovaEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClovaEmbeddingClient {

    private final WebClient clovaWebClient;

    @Value("${ncp.clova.embed-path}")
    private String embeddingApiUrl;

    /**
     * (기존) 텍스트 1개의 임베딩 벡터를 비동기적으로 반환합니다.
     */
    public Mono<List<Double>> getEmbedding(String text) {
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
                // ⭐️ (개선) 개별 요청에 타임아웃 설정
                .timeout(Duration.ofSeconds(10));
    }

    /**
     * (기존) 동기식(Blocking)
     */
    public List<Double> getEmbeddingSync(String text) {
        return getEmbedding(text).block(); // ⭐️ block()은 메인 스레드를 막으므로 배치 외엔 사용 주의
    }

    /**
     * 여러 텍스트를 병렬로 요청하고 결과 리스트를 반환합니다.
     *
     * @param texts 임베딩할 텍스트 리스트
     * @return 임베딩 벡터 리스트 (Mono<List<List<Double>>>)
     */
    public Mono<List<List<Double>>> getEmbeddingsParallel(List<String> texts) {
        // 1. 각 텍스트(String)를 비동기 작업(Mono)으로 변환합니다.
        return Flux.fromIterable(texts)
                // 2. 각 텍스트에 대해 getEmbedding(text) API 호출을 실행합니다.
                //    concurrency: 10 -> 동시에 최대 10개까지 병렬로 API를 호출합니다.
                .flatMap(text ->
                                getEmbedding(text)
                                        //  개별 작업 실패 시 전체가 멈추지 않고, 에러를 Mono.empty()로 변환
                                        .onErrorResume(e -> {
                                            log.error("개별 임베딩 실패 (텍스트: {}...): {}", text.substring(0, 20), e.getMessage());
                                            return Mono.empty(); // 실패한 것은 결과 리스트에서 제외됨
                                        })
                        , 10) // ️ Concurrency Level (동시 요청 수)
                // 3. 모든 병렬 작업이 완료되면, 성공한 결과(List<Double>)들을 다시 하나의 리스트(List<List<Double>>)로 수집합니다.
                .collectList();
    }

    /**
     *  병렬 요청 + 동기식(Blocking)
     */
    public List<List<Double>> getEmbeddingsParallelSync(List<String> texts) {
        return getEmbeddingsParallel(texts).block(); // 배치 작업에서 사용
    }
}
