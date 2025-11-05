package com.bobeat.backend.domain.search.repository;

import com.bobeat.backend.domain.search.entity.SearchHistory;
import java.util.List;

public interface SearchHistoryRepositoryCustom {
    List<SearchHistory> findByMemberWithCursor(Long memberId, String lastKnown, int limit);
}