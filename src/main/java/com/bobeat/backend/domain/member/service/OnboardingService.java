package com.bobeat.backend.domain.member.service;

import com.bobeat.backend.domain.member.dto.request.OnboardingRequest;
import com.bobeat.backend.domain.member.dto.response.OnBoardingResult;
import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.entity.MemberOnboardingProfile;
import com.bobeat.backend.domain.member.repository.MemberOnboardingProfileRepository;
import com.bobeat.backend.domain.member.repository.MemberRepository;
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
        if (memberId == null) {
            // Anonymous user - calculate level without saving to DB
            Level anonymousLevel = calculateLevelForAnonymousUser(request);
            return new OnBoardingResult(anonymousLevel.getValue());
        }

        Member member = memberRepository.findByIdOrElseThrow(memberId);

        MemberOnboardingProfile profile = createOrUpdateOnboardingProfile(member, request);

        Level level = profile.calculateAndUpdateHonbobLevel();

        onboardingProfileRepository.save(profile);

        return new OnBoardingResult(level.getValue());
    }

    private Level calculateLevelForAnonymousUser(OnboardingRequest request) {
        double totalWeightedScore = 0.0;

        for (var answer : request.answers()) {
            int questionNumber = answer.questionOrder();
            int score = answer.selectedOption();

            double weight = switch (questionNumber) {
                case 1 -> 1.2; // 혼밥 빈도
                case 2 -> 1.2; // 새로운 식당 도전
                case 3 -> 1.0; // 음식 선택 기준
                case 4 -> 1.3; // 혼밥 시 감정 (결정적 요인)
                case 5 -> 1.0; // 식사 동반자 선호
                default -> 1.0;
            };

            totalWeightedScore += score * weight;
        }

        return com.bobeat.backend.domain.member.entity.OnboardingAnswer.calculateLevel(totalWeightedScore);
    }

    private MemberOnboardingProfile createOrUpdateOnboardingProfile(Member member, OnboardingRequest request) {
        return onboardingProfileRepository.findByMemberId(member.getId())
                .map(existing -> {
                    existing.updateAnswers(request);
                    return existing;
                })
                .orElseGet(() -> MemberOnboardingProfile.create(member, request));
    }

    @Transactional
    public void deleteByMember(Member member) {
        onboardingProfileRepository.deleteByMember(member);
    }

}
