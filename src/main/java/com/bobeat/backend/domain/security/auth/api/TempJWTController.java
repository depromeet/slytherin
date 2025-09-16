package com.bobeat.backend.domain.security.auth.api;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.security.auth.dto.AuthResponse;
import com.bobeat.backend.domain.security.auth.service.JwtService;
import com.bobeat.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/temp/jwt")
@RequiredArgsConstructor
public class TempJWTController {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @PostMapping("/generate")
    public ApiResponse<AuthResponse> generateTokens(@RequestParam Long memberId) {
        log.info("임시 토큰 생성 요청 - memberId: {}", memberId);

        Member member = memberRepository.findByIdOrElseThrow(memberId);
        AuthResponse authResponse = jwtService.generateTokens(member);

        log.info("임시 토큰 생성 완료 - memberId: {}, accessToken 길이: {}, refreshToken 길이: {}",
                memberId, authResponse.getAccessToken().length(), authResponse.getRefreshToken().length());

        return ApiResponse.success(authResponse);
    }
}
