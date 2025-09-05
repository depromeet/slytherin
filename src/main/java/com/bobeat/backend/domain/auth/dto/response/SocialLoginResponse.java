package com.bobeat.backend.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 응답 DTO")
public record SocialLoginResponse(
        @Schema(description = "사용자 ID")
        String userId,
        @Schema(description = "닉네임")
        String nickname,
        @Schema(description = "혼밥 레벨")
        int level,
        @Schema(description = "액세스 토큰")
        String accessToken,
        @Schema(description = "리프레시 토큰")
        String refreshToken,
        @Schema(description = "액세스 토큰 만료 시간 (초)")
        long accessTokenExpiresIn,
        @Schema(description = "리프레시 토큰 만료 시간 (초)")
        long refreshTokenExpiresIn
) {
}
