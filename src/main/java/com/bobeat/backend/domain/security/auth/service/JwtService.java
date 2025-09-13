package com.bobeat.backend.domain.security.auth.service;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.security.auth.dao.RefreshTokenRepository;
import com.bobeat.backend.domain.security.auth.domain.RefreshToken;
import com.bobeat.backend.domain.security.auth.dto.AuthResponse;
import com.bobeat.backend.domain.security.auth.dto.TokenClaims;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";


    @Transactional
    public AuthResponse generateTokens(Member member) {
        TokenClaims tokenClaims = TokenClaims.from(member);
        String accessToken = generateAccessToken(tokenClaims);
        String refreshToken = generateRefreshToken(tokenClaims);

        try {
            // 비관적 락을 사용하여 동시성 문제 해결
            refreshTokenRepository.findByMemberIdWithLock(member.getId())
                    .ifPresentOrElse(
                            token -> token.updateRefreshToken(refreshToken),
                            () -> refreshTokenRepository.save(new RefreshToken(member.getId(), refreshToken))
                    );
        } catch (PessimisticLockingFailureException e) {
            throw new CustomException("토큰 생성 중 락 충돌 발생. memberId: " + member.getId() + " " + e, ErrorCode.CONCURRENCY_ERROR);
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiration(getClaims(accessToken).getExpiration())
                .refreshTokenExpiration(getClaims(refreshToken).getExpiration())
                .memberId(member.getId())
                .providerType(member.getSocialProvider())
                .email(member.getEmail() == null ? "" : member.getEmail())
                .profileImage(member.getProfileImageUrl())
                .memberRole(member.getRole())
                .build();
    }


    public String generateAccessToken(TokenClaims claims) {
        return generateToken(claims, accessTokenExpiration);
    }

    public String generateRefreshToken(TokenClaims claims) {
        return generateToken(claims, refreshTokenExpiration);
    }

    private String generateToken(TokenClaims tokenClaims, long expireTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireTime);

        Claims claims = Jwts.claims();
        claims.put("id", tokenClaims.getId());
        claims.put("role", tokenClaims.getRole().name());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        return resolveToken(bearerToken);
    }

    private String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    public String resolveAndValidateToken(String bearerToken) {
        String token = resolveToken(bearerToken);
        if (token == null) {
            throw new CustomException(ErrorCode.JWT_INVALID_TOKEN);
        }
        validateToken(token);
        return token;
    }

    public String resolveAndValidateToken(HttpServletRequest request) {
        String token = resolveToken(request);
        if (token == null) {
            throw new CustomException(ErrorCode.TOKEN_NOT_FOUND);
        }
        validateToken(token);
        return token;
    }

    public void validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())).build().parseClaimsJws(token);
        } catch (SecurityException | MalformedJwtException e) {
            throw new CustomException(ErrorCode.JWT_INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.JWT_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new CustomException(ErrorCode.JWT_UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.JWT_CLAIMS_EMPTY);
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Long userId = claims.get("id", Long.class);
        String role = claims.get("role", String.class);
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        return new UsernamePasswordAuthenticationToken(userId, "", authorities);
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())).build().parseClaimsJws(token).getBody();
    }
} 
