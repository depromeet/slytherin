package com.bobeat.backend.domain.search.controller;

import com.bobeat.backend.domain.search.service.SearchService;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ApiResponse<List<StoreSearchResultDto>> searchStore(@AuthenticationPrincipal Long memberId,
                                                               @RequestParam("query") String query) {
        List<StoreSearchResultDto> response = searchService.searchStore(memberId, query);
        return ApiResponse.success(response);
    }
}
