package com.bobeat.backend.domain.security.oauth.dto;

import com.bobeat.backend.domain.member.entity.SocialProvider;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoUserInfo implements OAuth2UserInfo {
    private String id;
    private String email;
    private String profileImageUrl;
    private String birthyear;

    public static KakaoUserInfo from(JsonNode response) {
        JsonNode kakaoAccount = response.get("kakao_account");
        JsonNode profile = kakaoAccount != null ? kakaoAccount.get("profile") : null;

        return KakaoUserInfo.builder()
                .id(response.get("id").asText())
                .email(kakaoAccount != null && kakaoAccount.has("email") ? kakaoAccount.get("email").asText() : "")
                .profileImageUrl(profile != null && profile.has("profile_image_url") ? profile.get("profile_image_url").asText() : "")
                .birthyear(kakaoAccount != null && kakaoAccount.has("birthyear") ? kakaoAccount.get("birthyear").asText() : "")
                .build();
    }

    @Override
    public String getProviderId() {
        return id;
    }

    @Override
    public String getEmail() {
        return this.email == null ? "" : this.email;
    }

    @Override
    public String getProfileImageUrl() {
        return profileImageUrl == null ? "" : profileImageUrl;
    }


    @Override
    public SocialProvider getProviderType() {
        return SocialProvider.KAKAO;
    }
}
