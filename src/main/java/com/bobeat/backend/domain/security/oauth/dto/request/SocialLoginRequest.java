package com.bobeat.backend.domain.security.oauth.dto.request;

import com.bobeat.backend.domain.member.entity.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 요청 DTO")
public record SocialLoginRequest(
        @Schema(description = "소셜 프로바이더", example = "KAKAO")
        SocialProvider provider,
        @Schema(description = "Access Token", example = "ya29.a0ARrdaM...")
        String oAuthToken
) {
}
