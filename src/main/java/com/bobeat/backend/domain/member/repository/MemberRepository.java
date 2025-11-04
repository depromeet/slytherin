package com.bobeat.backend.domain.member.repository;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.onboardingProfile WHERE m.id = :id")
    Optional<Member> findByIdWithOnboardingProfile(@Param("id") Long id);

    default Member findByMemberId(Long memberId) {
        return findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    default Member findByIdOrElseThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    default Member findByIdWithOnboardingProfileOrElseThrow(Long id) {
        return findByIdWithOnboardingProfile(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    Optional<Member> findByProviderId(String providerId);
}
