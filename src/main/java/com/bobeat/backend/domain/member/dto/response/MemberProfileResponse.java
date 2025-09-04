package com.bobeat.backend.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 프로필 조회 응답 DTO")
public record MemberProfileResponse(
        @Schema(description = "사용자 ID")
        String userId,
        @Schema(description = "닉네임")
        String nickname,
        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,
        @Schema(description = "혼밥 레벨")
        int level,
        @Schema(description = "저장한 가게 수")
        int scrapCount
) {
}
