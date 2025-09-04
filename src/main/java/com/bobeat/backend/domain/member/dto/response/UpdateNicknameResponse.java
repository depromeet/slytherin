package com.bobeat.backend.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 수정 응답 DTO")
public record UpdateNicknameResponse(
        @Schema(description = "수정된 닉네임")
        String nickName
) {
}
