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

    default Member findByIdOrElseThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    default Member findByIdWithOnboardingProfileOrElseThrow(Long id) {
        return findByIdWithOnboardingProfile(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    Optional<Member> findByProviderId(String providerId);

    /**
     * 닉네임 중복 여부를 확인합니다.
     *
     * 최적화: EXISTS 쿼리 사용으로 성능 향상
     * - COUNT 대신 EXISTS를 사용하여 첫 번째 매칭 행에서 즉시 반환
     * - 인덱스 스캔 최소화
     */
    @Query(value = "SELECT EXISTS(SELECT 1 FROM member WHERE nickname = :nickname)", nativeQuery = true)
    boolean existsByNickname(@Param("nickname") String nickname);
}
