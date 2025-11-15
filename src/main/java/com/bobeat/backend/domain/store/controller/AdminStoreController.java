package com.bobeat.backend.domain.store.controller;

import com.bobeat.backend.domain.store.dto.request.StoreCreateRequest;
import com.bobeat.backend.domain.store.dto.response.KakaoStoreResponse;
import com.bobeat.backend.domain.store.service.StoreCrawlingService;
import com.bobeat.backend.domain.store.service.StoreCreateService;
import com.bobeat.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "어드민 가게 관리", description = "어드민을 위한 가게 관리 API")
@RestController
@RequestMapping("/api/admin/stores")
@RequiredArgsConstructor
public class AdminStoreController {

    private final StoreCreateService storeCreateService;
    private final StoreCrawlingService storeCrawlingService;

    @Operation(summary = "가게 등록", description = "어드민이 새로운 가게를 등록합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<Long>> createStores(@Valid @RequestBody List<StoreCreateRequest> requests) {
        List<Long> storeIds = storeCreateService.createStores(requests);
        return ApiResponse.success(storeIds);
    }

    @Operation(summary = "가게 탐색", description = "어드민이 새로운 가게 정보를 탐색합니다")
    @GetMapping
    public ApiResponse<KakaoStoreResponse> findStore(@RequestParam("storeName") String storeName) {

        KakaoStoreResponse response = storeCrawlingService.findStore(storeName);
        return ApiResponse.success(response);
    }
}
