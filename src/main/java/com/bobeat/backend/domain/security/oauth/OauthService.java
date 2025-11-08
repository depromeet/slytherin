package com.bobeat.backend.domain.security.oauth;


import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.entity.MemberRole;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.security.auth.dto.AuthResponse;
import com.bobeat.backend.domain.security.auth.service.JwtService;
import com.bobeat.backend.domain.security.oauth.dto.OAuth2UserInfo;
import com.bobeat.backend.domain.security.oauth.dto.request.SocialLoginRequest;
import com.bobeat.backend.domain.security.oauth.service.OAuth2Factory;
import com.bobeat.backend.domain.security.oauth.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OauthService {

    private final MemberRepository memberRepository;
    private final OAuth2Factory oAuth2Factory;
    private final JwtService jwtService;
    private final NicknameGenerator NicknameGenerator;
    private final NicknameGenerator nicknameGenerator;

    @Value("${profile.default-image}")
    private String defaultProfileImage;

    @Transactional
    public AuthResponse login(SocialLoginRequest request) {
        OAuth2Service oAuth2Service = oAuth2Factory.getProvider(request.provider());
        OAuth2UserInfo userInfo = oAuth2Service.getUser(request.oAuthToken());

        Member member = findOrSignUp(userInfo);

        return jwtService.generateTokens(member);
    }

    private Member findOrSignUp(OAuth2UserInfo userInfo) {
        return memberRepository.findByProviderId(userInfo.getProviderId())
                .orElseGet(() -> saveMember(userInfo));
    }

    private Member saveMember(OAuth2UserInfo userInfo) {
        Member member = toEntity(userInfo);
        return memberRepository.save(member);
    }

    private Member toEntity(OAuth2UserInfo userInfo) {
        return Member.builder()
                .socialProvider(userInfo.getProviderType())
                .nickname(nicknameGenerator.getRandomNickname())
                .providerId(userInfo.getProviderId())
                .email(userInfo.getEmail())
                .role(MemberRole.USER)
                .profileImageUrl(userInfo.getProfileImageUrl() == "" ? defaultProfileImage : userInfo.getProfileImageUrl())
                .build();
    }
}
