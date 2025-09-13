package com.bobeat.backend.domain.security.oauth.dto;

import com.bobeat.backend.domain.member.entity.SocialProvider;

public interface OAuth2UserInfo {

    String getProviderId();

    SocialProvider getProviderType();

    String getEmail();

    String getProfileImageUrl();
}
