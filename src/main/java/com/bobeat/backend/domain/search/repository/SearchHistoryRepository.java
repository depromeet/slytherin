package com.bobeat.backend.domain.search.repository;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.search.entity.SearchHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    Optional<SearchHistory> findByQueryAndMember(String query, Member member);
}
