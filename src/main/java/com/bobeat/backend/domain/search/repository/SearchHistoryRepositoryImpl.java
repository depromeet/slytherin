package com.bobeat.backend.domain.search.repository;

import com.bobeat.backend.domain.search.entity.QSearchHistory;
import com.bobeat.backend.domain.search.entity.SearchHistory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SearchHistoryRepositoryImpl implements SearchHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SearchHistory> findByMemberWithCursor(Long memberId, String lastKnown, int limit) {
        QSearchHistory searchHistory = QSearchHistory.searchHistory;
        BooleanExpression cursorCondition = null;
        if (lastKnown != null) {
            LocalDateTime lastCursorTime = LocalDateTime.parse(lastKnown);
            cursorCondition = searchHistory.updatedAt.lt(lastCursorTime);
        }

        return queryFactory
                .selectFrom(searchHistory)
                .where(
                        searchHistory.member.id.eq(memberId),
                        cursorCondition
                )
                .orderBy(searchHistory.updatedAt.desc())
                .limit(limit)
                .fetch();
    }
}