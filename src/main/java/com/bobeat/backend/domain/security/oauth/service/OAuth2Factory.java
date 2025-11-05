package com.bobeat.backend.domain.security.oauth.service;

import com.bobeat.backend.domain.member.entity.SocialProvider;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2Factory {

    private final KakaoService kakaoService;
    private final GoogleService googleService;
    private final AppleService appleService;

    public OAuth2Service getProvider(SocialProvider socialProvider) {
        return switch (socialProvider) {
            case KAKAO -> kakaoService;
            case GOOGLE -> googleService;
            case APPLE -> appleService;
            default -> throw new CustomException(ErrorCode.PROVIDER_NOT_SUPPORTED);
        };
    }
}
