package com.bobeat.backend.domain.member.entity;

import com.bobeat.backend.domain.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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

    private Integer question1;
    private Integer question2;
    private Integer question3;
    private Integer question4;
    private Integer question5;

    private Integer honbapLevel;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}
