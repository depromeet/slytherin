package com.bobeat.backend.domain.member.repository;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.entity.MemberOnboardingProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberOnboardingProfileRepository extends JpaRepository<MemberOnboardingProfile, Long> {
    Optional<MemberOnboardingProfile> findByMemberId(Long memberId);

    void deleteByMember(Member member);
}
