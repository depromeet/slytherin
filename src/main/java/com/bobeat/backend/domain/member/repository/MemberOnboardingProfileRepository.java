package com.bobeat.backend.domain.member.repository;

import com.bobeat.backend.domain.member.entity.MemberOnboardingProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberOnboardingProfileRepository extends JpaRepository<MemberOnboardingProfile, Long> {
    Optional<MemberOnboardingProfile> findByMemberId(Long memberId);
}
