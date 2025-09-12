package com.bobeat.backend.domain.member.entity;

import com.bobeat.backend.domain.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private SocialProvider socialProvider;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
    private MemberOnboardingProfile onboardingProfile;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
