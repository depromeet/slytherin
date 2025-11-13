package com.bobeat.backend.domain.report.repository;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.report.entity.StoreReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreReportRepository extends JpaRepository<StoreReport, Long> {
    void deleteByMember(Member member);
}