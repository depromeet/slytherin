package com.bobeat.backend.domain.member.entity;

import com.bobeat.backend.domain.common.BaseTimeEntity;
import com.bobeat.backend.domain.member.dto.request.OnboardingRequest;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_onboarding_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberOnboardingProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OnboardingAnswer question1;

    @Enumerated(EnumType.STRING)
    private OnboardingAnswer question2;

    @Enumerated(EnumType.STRING)
    private OnboardingAnswer question3;

    @Enumerated(EnumType.STRING)
    private OnboardingAnswer question4;

    @Enumerated(EnumType.STRING)
    private OnboardingAnswer question5;

    @Enumerated(EnumType.STRING)
    private Level honbapLevel = Level.LEVEL_1;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public static MemberOnboardingProfile create(Member member, OnboardingRequest request) {
        MemberOnboardingProfile profile = new MemberOnboardingProfile();
        profile.member = member;
        profile.updateAnswers(request);
        return profile;
    }

    public void updateAnswers(OnboardingRequest request) {
        request.answers().forEach(answer -> {
            OnboardingAnswer onboardingAnswer = OnboardingAnswer.findByQuestionAndOption(
                    answer.questionOrder(), answer.selectedOption());
            setAnswerByQuestionNumber(answer.questionOrder(), onboardingAnswer);
        });
    }

    private void setAnswerByQuestionNumber(int questionNumber, OnboardingAnswer answer) {
        switch (questionNumber) {
            case 1 -> this.question1 = answer;
            case 2 -> this.question2 = answer;
            case 3 -> this.question3 = answer;
            case 4 -> this.question4 = answer;
            case 5 -> this.question5 = answer;
            default -> throw new CustomException(ErrorCode.INVALID_ONBOARDING_QUESTION);
        }
    }

    public Level calculateAndUpdateHonbapLevel() {
        double totalWeightedScore = calculateTotalWeightedScore();
        Level level = OnboardingAnswer.calculateLevel(totalWeightedScore);
        this.honbapLevel = level;
        return level;
    }

    private double calculateTotalWeightedScore() {
        double score = 0.0;

        if (question1 != null) score += question1.getWeightedScore();
        if (question2 != null) score += question2.getWeightedScore();
        if (question3 != null) score += question3.getWeightedScore();
        if (question4 != null) score += question4.getWeightedScore();
        if (question5 != null) score += question5.getWeightedScore();

        return score;
    }

}
