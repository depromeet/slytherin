package com.bobeat.backend.domain.store.controller;

import com.bobeat.backend.domain.search.dto.response.SearchHistoryDto;
import com.bobeat.backend.domain.store.dto.request.EditProposalRequest;
import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreDetailResponse;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.service.StoreService;
import com.bobeat.backend.global.response.ApiResponse;
import com.bobeat.backend.global.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Store", description = "가게(식당) 관련 API")
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {
    private final StoreService storeService;

    @Operation(summary = "위치 기반 식당 검색", description = "현재 위치를 기반으로 식당을 검색하고 필터링합니다.")
    @PostMapping
    public ApiResponse<CursorPageResponse<StoreSearchResultDto>> searchStores(
            @RequestBody @Valid StoreFilteringRequest request) {
        log.info("get store list by location: {}", request);
        CursorPageResponse<StoreSearchResultDto> response = storeService.search(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "식당 상세 정보 조회", description = "특정 식당의 상세 정보를 조회합니다.")
    @GetMapping("/{storeId}")
    public ApiResponse<StoreDetailResponse> getStoreDetails(
            @Parameter(description = "식당 ID") @PathVariable("storeId") Long storeId
    ) {
        StoreDetailResponse response = storeService.findById(storeId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "정보 수정 제안", description = "식당 정보 수정을 제안합니다.")
    @PostMapping("/{storeId}/proposals")
    public ApiResponse<Void> proposeEdit(
            @Parameter(description = "식당 ID") @PathVariable Long storeId,
            @RequestBody EditProposalRequest request
    ) {
        return ApiResponse.successOnly();
    }

    @Operation(summary = "식당 검색 히스토리 조회", description = "사용자의 검색 히스토리를 날짜 기준 내림차순으로 제공")
    @GetMapping("/history")
    public ApiResponse<List<SearchHistoryDto>> getSearchHistory() {
        List<SearchHistoryDto> response = storeService.findSearchHistory();
        return ApiResponse.success(response);
    }
}
