package com.bobeat.backend.domain.store.controller;

import com.bobeat.backend.domain.store.dto.request.EditProposalRequest;
import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreDetailResponse;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResponse;
import com.bobeat.backend.domain.store.service.StoreService;
import com.bobeat.backend.global.response.ApiResponse;
import com.bobeat.backend.global.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Store", description = "가게(식당) 관련 API")
@RestController
@RequestMapping("/api/v1/store")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    @Operation(summary = "위치 기반 식당 검색", description = "현재 위치를 기반으로 식당을 검색하고 필터링합니다.")
    @PostMapping
    public ApiResponse<CursorPageResponse<StoreSearchResponse>> searchRestaurants(
            @RequestBody @Valid StoreFilteringRequest request) {
        CursorPageResponse<StoreSearchResponse> response = storeService.search(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "식당 상세 정보 조회", description = "특정 식당의 상세 정보를 조회합니다.")
    @GetMapping("/{storeId}")
    public ApiResponse<StoreDetailResponse> getRestaurantDetails(
            @Parameter(description = "식당 ID") @PathVariable Long storeId
    ) {
        return ApiResponse.success(null);
    }

    @Operation(summary = "정보 수정 제안", description = "식당 정보 수정을 제안합니다.")
    @PostMapping("/{storeId}/proposals")
    public ApiResponse<Void> proposeEdit(
            @Parameter(description = "식당 ID") @PathVariable Long storeId,
            @RequestBody EditProposalRequest request
    ) {
        return ApiResponse.successOnly();
    }
}
