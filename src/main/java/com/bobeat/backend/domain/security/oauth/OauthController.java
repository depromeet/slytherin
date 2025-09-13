package com.bobeat.backend.domain.security.oauth;

import com.bobeat.backend.domain.security.auth.dto.AuthResponse;
import com.bobeat.backend.domain.security.oauth.dto.request.SocialLoginRequest;
import com.bobeat.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "oauth", description = "소셜 로그인 및 인증 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth")
public class OauthController {

    private final OauthService oauthService;

    @Operation(summary = "소셜 로그인", description = "카카오, 구글 Access Token으로 로그인 또는 회원가입을 처리합니다.")
    @PostMapping("/social-login")
    public ApiResponse<AuthResponse> socialLogin(@RequestBody SocialLoginRequest request) {
        AuthResponse response = oauthService.login(request);
        return ApiResponse.success(response);
    }
}
