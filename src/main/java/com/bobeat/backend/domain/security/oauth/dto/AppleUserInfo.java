package com.bobeat.backend.domain.security.oauth.dto;

import com.bobeat.backend.domain.member.entity.SocialProvider;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class AppleUserInfo implements OAuth2UserInfo {

    private String providerId;
    private String email;

    public static AppleUserInfo from(JsonNode response) {
        return AppleUserInfo.builder()
                .providerId(response.get("sub").asText())
                .email(response.has("email") ? response.get("email").asText() : "")
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
