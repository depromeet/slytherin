package com.bobeat.backend.domain.security.oauth.dto;

import com.bobeat.backend.domain.member.entity.SocialProvider;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

@Builder
public class GoogleUserInfo implements OAuth2UserInfo {

    private String providerId;
    private String email;
    private String profileImageUrl;

    public static GoogleUserInfo from(JsonNode response) {
        return GoogleUserInfo.builder()
                .providerId(response.get("id").asText())
                .email(response.has("email") ? response.get("email").asText() : "")
                .profileImageUrl(response.has("picture") ? response.get("picture").asText() : "")
                .build();
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public SocialProvider getProviderType() {
        return SocialProvider.GOOGLE;
    }

    @Override
    public String getEmail() {
        return this.email == null ? "" : this.email;
    }


    @Override
    public String getProfileImageUrl() {
        return profileImageUrl == null ? "" : this.profileImageUrl;
    }
} 
