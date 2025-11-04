package com.bobeat.backend.domain.store.controller;

import com.bobeat.backend.domain.store.service.StoreEmbeddingService;
import com.bobeat.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/embedding")
@RequiredArgsConstructor
@Tag(name = "임베딩 테스트", description = "Store 임베딩 테스트용 API")
public class StoreEmbeddingTestController {

    private final StoreEmbeddingService storeEmbeddingService;


    @GetMapping("/store/{storeId}")
    @Operation(summary = "Store 임베딩 생성 (동기)", description = "특정 Store의 임베딩 벡터를 생성 및 저장하고 결과를 반환합니다.")
    public ApiResponse<EmbeddingTestResponse> testStoreEmbedding(@PathVariable Long storeId) {

        EmbeddingTestResponse response = storeEmbeddingService.saveEmbeddingByStore(storeId);
        return ApiResponse.success(response);
    }

    @GetMapping("/store/{storeId}/text")
    @Operation(summary = "Store 임베딩 텍스트 확인", description = "임베딩에 사용되는 텍스트만 확인합니다.")
    public ApiResponse<StoreTextResponse> getStoreText(@PathVariable Long storeId) {
        StoreTextResponse response = storeEmbeddingService.createEmbeddingTextByStore(storeId);
        return ApiResponse.success(response);
    }

    @Getter
    @AllArgsConstructor
    public static class EmbeddingTestResponse {
        private Long storeId;
        private String storeName;
        private String combinedText;
        private List<Float> embedding;
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
