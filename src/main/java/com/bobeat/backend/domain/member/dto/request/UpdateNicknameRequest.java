package com.bobeat.backend.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "닉네임 수정 요청 DTO")
public record UpdateNicknameRequest(
        @Schema(description = "새로운 닉네임")
        @NotBlank(message = "닉네임은 필수 입력값입니다")
        String nickname
) {
}
