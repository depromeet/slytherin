package com.bobeat.backend.domain.search.dto.response;

import com.bobeat.backend.domain.search.entity.SearchHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record StoreSearchHistoryResponse(@Schema(description = "검색 ID")
                                         Long id,
                                         @Schema(description = "검색어")
                                         String query,
                                         @Schema(description = "생성 시간")
                                         LocalDateTime updateAt) {

    public static StoreSearchHistoryResponse from(SearchHistory searchHistory) {
        return new StoreSearchHistoryResponse(searchHistory.getId(), searchHistory.getQuery(),
                searchHistory.getUpdatedAt());
    }
}
