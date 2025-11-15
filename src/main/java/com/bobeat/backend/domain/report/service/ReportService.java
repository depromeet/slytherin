package com.bobeat.backend.domain.report.service;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.report.dto.request.StoreReportRequest;
import com.bobeat.backend.domain.report.entity.StoreReport;
import com.bobeat.backend.domain.report.repository.StoreReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StoreReportRepository storeReportRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void reportStore(StoreReportRequest request) {
        Member member = null;

        // memberId가 있는 경우 회원 조회
        if (request.memberId() != null) {
            member = memberRepository.findByIdOrElseThrow(request.memberId());
        }

        StoreReport storeReport = StoreReport.builder()
                .location(request.location())
                .name(request.name())
                .seatTypes(request.seatType())
                .paymentMethods(request.paymentMethods())
                .menuCategories(request.menuCategories())
                .recommendedMenu(request.recommendedMenu())
                .reason(request.reason())
                .member(member)
                .build();

        storeReportRepository.save(storeReport);
    }

    @Transactional
    public void deleteByMember(Member member) {
        storeReportRepository.deleteByMember(member);
    }
}
