package com.bobeat.backend.domain.security.oauth.service;

import com.bobeat.backend.domain.security.oauth.dto.OAuth2UserInfo;

public interface OAuth2Service {

    OAuth2UserInfo getUser(String accessToken);
}
