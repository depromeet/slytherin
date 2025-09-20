package com.bobeat.backend.domain.member.entity;

import com.bobeat.backend.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
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

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    private String providerId;

    private String email;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isOwner(Long memberId) {
        return this.id.equals(memberId);
    }
}
