package com.bobeat.backend.domain.security.oauth.dto;

import com.bobeat.backend.domain.member.entity.SocialProvider;
import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class AppleUserInfo implements OAuth2UserInfo {

    private String providerId;
    private String email;

    public static AppleUserInfo from(Claims claims) {
        String sub = claims.getSubject();
        String email = (String) claims.get("email");
        return AppleUserInfo.builder()
                .providerId(sub)
                .email(email)
                .build();
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public SocialProvider getProviderType() {
        return SocialProvider.APPLE;
    }

    @Override
    public String getEmail() {
        return this.email == null ? "" : this.email;
    }

    @Override
    public String getProfileImageUrl() {
        return "";
    }
}
