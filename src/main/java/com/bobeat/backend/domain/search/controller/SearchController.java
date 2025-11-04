package com.bobeat.backend.domain.search.controller;

import com.bobeat.backend.domain.search.dto.response.StoreSearchHistoryResponse;
import com.bobeat.backend.domain.search.service.SearchService;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.global.request.CursorPaginationRequest;
import com.bobeat.backend.global.response.ApiResponse;
import com.bobeat.backend.global.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Search", description = "검색 관련 API")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "식당 검색", description = "식당 검색 및 검색 결과를 반환한다")
    @GetMapping()
    public ApiResponse<CursorPageResponse<StoreSearchResultDto>> searchStore(@AuthenticationPrincipal Long memberId,
                                                                             @RequestParam("query") String query,
                                                                             CursorPaginationRequest paging) {
        CursorPageResponse<StoreSearchResultDto> response = searchService.searchStoreV2(memberId, query, paging);
        return ApiResponse.success(response);
    }

    @Operation(summary = "검색 히스토리 조회", description = "식당 검색 히스토리를 조회한다")
    @PostMapping("")
    public ApiResponse<List<StoreSearchHistoryResponse>> getStoreSearchHistory(@AuthenticationPrincipal Long memberId,
                                                                               @RequestBody CursorPaginationRequest paging) {
        List<StoreSearchHistoryResponse> response = searchService.getStoreSearchHistory(memberId, paging);
        return ApiResponse.success(response);
    }

    @Operation(summary = "검색 히스토리 전체 삭제", description = "식당 검색 히스토리를 모두 삭제한다")
    @DeleteMapping
    public ApiResponse<Void> deleteStoreSearchHistory(@AuthenticationPrincipal Long memberId) {
        searchService.deleteStoreSearchHistory(memberId);
        return ApiResponse.successOnly();
    }

    @Operation(summary = "검색 히스토리 삭제", description = "식당 검색 히스토리를 모두 삭제한다")
    @DeleteMapping("{searchHistoryId}")
    public ApiResponse<Void> deleteStoreSearchHistory(@AuthenticationPrincipal Long memberId,
                                                      @PathVariable("searchHistoryId") Long searchHistoryId) {
        searchService.deleteStoreSearchHistoryById(memberId, searchHistoryId);
        return ApiResponse.successOnly();
    }
}
