package com.bobeat.backend.domain.store.controller;

import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.domain.store.service.StoreEmbeddingService;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/test/embedding")
@RequiredArgsConstructor
@Tag(name = "임베딩 테스트", description = "Store 임베딩 테스트용 API")
public class StoreEmbeddingTestController {

    private final StoreEmbeddingService storeEmbeddingService;
    private final StoreRepository storeRepository;

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Store 임베딩 생성 (동기)", description = "특정 Store의 임베딩 벡터를 생성하고 결과를 반환합니다.")
    public EmbeddingTestResponse testStoreEmbedding(@PathVariable Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STORE));

        String combinedText = storeEmbeddingService.buildStoreText(store);
        List<Double> embedding = storeEmbeddingService.generateStoreEmbeddingSync(store);

        return new EmbeddingTestResponse(
                storeId,
                store.getName(),
                combinedText,
                embedding,
                embedding.size()
        );
    }

    @GetMapping("/store/{storeId}/async")
    @Operation(summary = "Store 임베딩 생성 (비동기)", description = "특정 Store의 임베딩 벡터를 비동기로 생성하고 결과를 반환합니다.")
    public Mono<EmbeddingTestResponse> testStoreEmbeddingAsync(@PathVariable Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STORE));

        String combinedText = storeEmbeddingService.buildStoreText(store);

        return storeEmbeddingService.generateStoreEmbedding(store)
                .map(embedding -> new EmbeddingTestResponse(
                        storeId,
                        store.getName(),
                        combinedText,
                        embedding,
                        embedding.size()
                ));
    }

    @GetMapping("/store/{storeId}/text")
    @Operation(summary = "Store 임베딩 텍스트 확인", description = "임베딩에 사용되는 텍스트만 확인합니다.")
    public StoreTextResponse getStoreText(@PathVariable Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STORE));

        String combinedText = storeEmbeddingService.buildStoreText(store);

        return new StoreTextResponse(
                storeId,
                store.getName(),
                combinedText,
                combinedText.length()
        );
    }

    @Getter
    @AllArgsConstructor
    public static class EmbeddingTestResponse {
        private Long storeId;
        private String storeName;
        private String combinedText;
        private List<Double> embedding;
        private int embeddingDimension;
    }

    @Getter
    @AllArgsConstructor
    public static class StoreTextResponse {
        private Long storeId;
        private String storeName;
        private String combinedText;
        private int textLength;
    }
}
