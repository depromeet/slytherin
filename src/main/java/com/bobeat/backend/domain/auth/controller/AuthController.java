package com.bobeat.backend.domain.auth.controller;

import com.bobeat.backend.domain.auth.dto.request.SocialLoginRequest;
import com.bobeat.backend.domain.auth.dto.response.SocialLoginResponse;
import com.bobeat.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Operation(summary = "소셜 로그인", description = "카카오, 구글 토큰으로 로그인 또는 회원가입을 처리합니다.")
    @PostMapping("/social-login")
    public ApiResponse<SocialLoginResponse> socialLogin(@RequestBody SocialLoginRequest request) {
        return ApiResponse.success(null);
    }
}
