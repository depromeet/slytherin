package com.bobeat.backend.domain.security.auth.service;


import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.security.auth.dao.RefreshTokenRepository;
import com.bobeat.backend.domain.security.auth.domain.RefreshToken;
import com.bobeat.backend.domain.security.auth.dto.AuthResponse;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse renewAccessToken(String refreshTokenWithBearer) {
        String refreshToken = jwtService.resolveAndValidateToken(refreshTokenWithBearer);

        // 토큰 검증 후 Claims 한번만 추출
        Claims claims = jwtService.getClaims(refreshToken);
        Long memberId = claims.get("id", Long.class);
        Date expiration = claims.getExpiration();

        // 토큰 만료일 확인
        Date now = new Date();
        long remainingTimeMs = expiration.getTime() - now.getTime();
        long remainingDays = remainingTimeMs / (1000 * 60 * 60 * 24);
        long remainingHours = (remainingTimeMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);

        log.info("토큰 갱신 요청 - memberId: {}, 토큰 만료일: {}, 남은 기간: {}일 {}시간",
                memberId, expiration, remainingDays, remainingHours);

        try {
            // 비관적 락을 사용하여 동시성 문제 해결
            Optional<RefreshToken> storedTokenOpt = refreshTokenRepository.findByMemberIdWithLock(memberId);
            
            if (storedTokenOpt.isEmpty()) {
                throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
            }
            
            RefreshToken storedToken = storedTokenOpt.get();
            String storedRefreshToken = storedToken.getRefreshToken();
            
            log.info("토큰 비교 - memberId: {}, DB토큰: {}, 요청토큰: {}",
                    memberId, 
                    storedRefreshToken,
                    refreshToken);
            
            if (!storedRefreshToken.equals(refreshToken)) {
                log.warn("리프레시 토큰이 일치하지 않습니다. memberId: {}, DB토큰길이: {}, 요청토큰길이: {}", 
                        memberId, storedRefreshToken.length(), refreshToken.length());
                throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
            }

            Member member = memberRepository.findByIdOrElseThrow(memberId);

            log.info("토큰 갱신 성공 - memberId: {}", memberId);
            return jwtService.generateTokens(member);
            
        } catch (PessimisticLockingFailureException e) {
            throw new CustomException("리프레시 토큰 갱신 중 락 충돌 발생. memberId: " + memberId + " " + e, ErrorCode.CONCURRENCY_ERROR);
        }
    }


    @Transactional
    public void logout(String accessTokenWithBearer) {
        String accessToken = jwtService.resolveAndValidateToken(accessTokenWithBearer);

        Claims claims = jwtService.getClaims(accessToken);
        Long memberId = claims.get("id", Long.class);

        refreshTokenRepository.findByMemberId(memberId)
                .ifPresent(refreshTokenRepository::delete);

        log.info("사용자 로그아웃 완료. memberId: {}", memberId);
    }

    @Transactional(readOnly = true)
    public String getTokenStatus(Long memberId) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByMemberId(memberId);
        
        if (tokenOpt.isEmpty()) {
            return "토큰 없음";
        }
        
        RefreshToken token = tokenOpt.get();
        String refreshToken = token.getRefreshToken();
        
        return String.format("토큰 존재 - 길이: %d, 끝 4자리: %s", 
                refreshToken.length(), 
                refreshToken.substring(Math.max(0, refreshToken.length() - 4)));
    }
}
