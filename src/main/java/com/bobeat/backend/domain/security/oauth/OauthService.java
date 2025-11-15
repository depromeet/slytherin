package com.bobeat.backend.domain.security.oauth;


import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.entity.MemberRole;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.member.service.OnboardingService;
import com.bobeat.backend.domain.report.service.ReportService;
import com.bobeat.backend.domain.review.service.ReviewService;
import com.bobeat.backend.domain.search.service.SearchService;
import com.bobeat.backend.domain.security.auth.dao.RefreshTokenRepository;
import com.bobeat.backend.domain.security.auth.dto.AuthResponse;
import com.bobeat.backend.domain.security.auth.service.JwtService;
import com.bobeat.backend.domain.security.oauth.dto.OAuth2UserInfo;
import com.bobeat.backend.domain.security.oauth.dto.request.OauthUnlinkRequest;
import com.bobeat.backend.domain.security.oauth.dto.request.SocialLoginRequest;
import com.bobeat.backend.domain.security.oauth.service.OAuth2Factory;
import com.bobeat.backend.domain.security.oauth.service.OAuth2Service;
import com.bobeat.backend.domain.store.service.StoreProposalService;
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
    private final NicknameGenerator nicknameGenerator;
    private final OnboardingService onboardingService;
    private final ReportService reportService;
    private final ReviewService reviewService;
    private final SearchService searchService;
    private final StoreProposalService storeProposalService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${profile.default-image}")
    private String defaultProfileImage;

    @Transactional
    public AuthResponse login(SocialLoginRequest request) {
        OAuth2Service oAuth2Service = oAuth2Factory.getProvider(request.provider());
        OAuth2UserInfo userInfo = oAuth2Service.getUser(request.oAuthToken());

        Member member = findOrSignUp(userInfo);

        return jwtService.generateTokens(member);
    }

    public void unlinkLogin(Long memberId, OauthUnlinkRequest request) {
        OAuth2Service oAuth2Service = oAuth2Factory.getProvider(request.provider());
        oAuth2Service.unlink(request.oAuthToken());
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        onboardingService.deleteByMember(member);
        reportService.deleteByMember(member);
        reviewService.deleteByMember(member);
        searchService.deleteByMember(member);
        storeProposalService.deleteByMember(member);
        refreshTokenRepository.deleteByMemberId(memberId);
        memberRepository.delete(member);
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
                .profileImageUrl(
                        userInfo.getProfileImageUrl() == "" ? defaultProfileImage : userInfo.getProfileImageUrl())
                .build();
    }
}
