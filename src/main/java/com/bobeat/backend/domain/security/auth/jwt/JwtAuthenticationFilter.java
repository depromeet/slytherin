package com.bobeat.backend.domain.security.auth.jwt;

import com.bobeat.backend.domain.security.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = jwtService.resolveAndValidateToken(request);
        Authentication authentication = jwtService.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String bearerToken = request.getHeader("Authorization");

        // OPTIONS 요청은 항상 허용 (CORS preflight)
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 로그인 API는 JWT 검증에서 제외
        if (requestURI.startsWith("/api/v1/auth/renew") || requestURI.startsWith("/api/v1/oauth/")) {
            return true;
        }

        // Authorization 헤더가 없거나 Bearer로 시작하지 않으면 필터 건너뛰기
        return bearerToken == null || !bearerToken.startsWith("Bearer ");
    }
}
