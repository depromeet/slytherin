package com.bobeat.backend.domain.security.auth.api;

import com.bobeat.backend.domain.security.auth.dto.AuthResponse;
import com.bobeat.backend.domain.security.auth.service.AuthService;
import com.bobeat.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "jwt", description = "JWT 토큰 발급 관련 API")
public class JwtTokenController {

    private final AuthService authService;


    @PostMapping("/renew")
    public ApiResponse<AuthResponse> renewToken(@RequestHeader("Authorization") String refreshTokenWithBearer) {
        return ApiResponse.success(authService.renewAccessToken(refreshTokenWithBearer));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String accessTokenWithBearer) {
        authService.logout(accessTokenWithBearer);
        return ApiResponse.successOnly();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/token-status")
    public ApiResponse<String> getTokenStatus(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.success(authService.getTokenStatus(memberId));
    }
}
