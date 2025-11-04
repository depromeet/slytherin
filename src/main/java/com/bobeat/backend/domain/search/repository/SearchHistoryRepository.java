package com.bobeat.backend.domain.search.repository;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.search.entity.SearchHistory;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long>, SearchHistoryRepositoryCustom {

    Optional<SearchHistory> findByQueryAndMember(String query, Member member);

    Optional<SearchHistory> findById(Long id);

    default SearchHistory findBySearchHistoryId(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SEARCH_HISTORY_NOT_FOUND));
    }

    void deleteByMember(Member member);
}
