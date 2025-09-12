package com.bobeat.backend.domain.member.service;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.entity.MemberOnboardingProfile;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.member.dto.request.OnboardingRequest;
import com.bobeat.backend.domain.member.dto.response.OnBoardingResult;
import com.bobeat.backend.domain.member.repository.MemberOnboardingProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final MemberRepository memberRepository;
    private final MemberOnboardingProfileRepository onboardingProfileRepository;

    @Transactional
    public OnBoardingResult submitOnboarding(Long memberId, OnboardingRequest request) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        
        MemberOnboardingProfile profile = createOrUpdateOnboardingProfile(member, request);
        
        Level level = profile.calculateAndUpdateHonbapLevel();
        
        onboardingProfileRepository.save(profile);
        
        return new OnBoardingResult(level.getValue());
    }

    private MemberOnboardingProfile createOrUpdateOnboardingProfile(Member member, OnboardingRequest request) {
        return onboardingProfileRepository.findByMemberId(member.getId())
                .map(existing -> {
                    existing.updateAnswers(request);
                    return existing;
                })
                .orElseGet(() -> MemberOnboardingProfile.create(member, request));
    }

}
