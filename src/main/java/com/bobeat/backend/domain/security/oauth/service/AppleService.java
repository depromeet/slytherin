package com.bobeat.backend.domain.security.oauth.service;

import com.bobeat.backend.domain.security.oauth.domain.ApplePublicKey;
import com.bobeat.backend.domain.security.oauth.dto.AppleUserInfo;
import com.bobeat.backend.domain.security.oauth.dto.OAuth2UserInfo;
import com.bobeat.backend.domain.security.oauth.repository.ApplePublicKeyRepository;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppleService implements OAuth2Service {

    private static final String APPLE_KEY_ENDPOINT = "https://appleid.apple.com/auth/keys";
    private final WebClient webClient;
    private final ApplePublicKeyRepository applePublicKeyRepository;

    @Override
    public OAuth2UserInfo getUser(String idToken) {
        List<ApplePublicKey> applePublicKeys = applePublicKeyRepository.findAll();
        List<RSAPublicKey> rsaPublicKeys = applePublicKeys.stream()
                .map(ApplePublicKey::getEncodedPublicKey)
                .map(this::decodingRSAKey)
                .toList();
        for (RSAPublicKey rsaPublicKey : rsaPublicKeys) {
            try {
                Jws<Claims> jws = Jwts.parserBuilder()
                        .setSigningKey(rsaPublicKey)
                        .build()
                        .parseClaimsJws(idToken);
                Claims claims = jws.getBody();
                return AppleUserInfo.from(claims);

            } catch (io.jsonwebtoken.SignatureException | io.jsonwebtoken.MalformedJwtException |
                     IllegalArgumentException e) {
                log.debug("Apple ID 토큰 서명 검증 실패. 다른 키로 계속 진행합니다.", e);
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("만료된 Apple ID 토큰입니다.", e);
                throw new CustomException(ErrorCode.JWT_EXPIRED);
            } catch (Exception ex) {
                log.warn("Apple ID 토큰 처리 중 예상치 못한 오류가 발생했습니다.", ex);

            }
        }

        List<JsonNode> jsonNodes = fetchAppleKeyEndpoint();
        for (JsonNode jsonNode : jsonNodes) {
            try {
                String n = jsonNode.get("n").asText();
                String e = jsonNode.get("e").asText();
                RSAPublicKey rsaPublicKey = generateRSAPublicKey(n, e);
                Jws<Claims> jws = Jwts.parserBuilder()
                        .setSigningKey(rsaPublicKey)
                        .build()
                        .parseClaimsJws(idToken);
                Claims claims = jws.getBody();
                return AppleUserInfo.from(claims);
            } catch (io.jsonwebtoken.SignatureException | io.jsonwebtoken.MalformedJwtException |
                     IllegalArgumentException e) {
                log.debug("Apple ID 토큰 서명 검증 실패. 다른 키로 계속 진행합니다.", e);
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("만료된 Apple ID 토큰입니다.", e);
                throw new CustomException(ErrorCode.JWT_EXPIRED);
            } catch (Exception ex) {
                log.warn("Apple ID 토큰 처리 중 예상치 못한 오류가 발생했습니다.", ex);
            }
        }
        throw new CustomException(ErrorCode.APPLE_TOKEN_VALIDATION_FAIL);
    }

    private List<JsonNode> fetchAppleKeyEndpoint() {
        return webClient.get()
                .uri(APPLE_KEY_ENDPOINT)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(jsonNode -> {
                    List<JsonNode> list = new ArrayList<>();
                    jsonNode.get("keys").forEach(list::add);
                    return list;
                })
                .block();
    }

    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void refreshAppleKeys() {
        applePublicKeyRepository.deleteAll();

        List<JsonNode> jsonNodes = fetchAppleKeyEndpoint();

        List<ApplePublicKey> applePublicKeys = jsonNodes.stream()
                .map(jsonNode -> {
                    RSAPublicKey rsaPublicKey = generateRSAPublicKey(jsonNode);
                    String encodedKey = Base64.getEncoder().encodeToString(rsaPublicKey.getEncoded());

                    return ApplePublicKey.builder()
                            .kty(jsonNode.get("kty").asText())
                            .kid(jsonNode.get("kid").asText())
                            .alg(jsonNode.get("alg").asText())
                            .n(jsonNode.get("n").asText())
                            .e(jsonNode.get("e").asText())
                            .encodedPublicKey(encodedKey)
                            .build();
                })
                .toList();
        applePublicKeyRepository.saveAll(applePublicKeys);
    }

    public RSAPublicKey generateRSAPublicKey(JsonNode jsonNode) {
        String n = jsonNode.get("n").asText();
        String e = jsonNode.get("e").asText();
        return generateRSAPublicKey(n, e);
    }

    private RSAPublicKey generateRSAPublicKey(String n, String e) {
        byte[] nBytes = Base64.getUrlDecoder().decode(n);
        byte[] eBytes = Base64.getUrlDecoder().decode(e);

        BigInteger modulus = new BigInteger(1, nBytes);
        BigInteger exponent = new BigInteger(1, eBytes);

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
        } catch (Exception exception) {
            log.error("애플 공개키 생성 중 문제가 발생했습니다.", exception);
            throw new CustomException("애플 공개키 생성중 문제가 발생했습니다", ErrorCode.INTERNAL_SERVER);
        }
    }

    private RSAPublicKey decodingRSAKey(String encodedKey) {
        byte[] decoded = Base64.getDecoder().decode(encodedKey);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception exception) {
            log.error("공개키 디코딩중 문제가 발생했습니다.", exception);
            throw new CustomException("애플 공개키 생성중 문제가 발생했습니다", ErrorCode.INTERNAL_SERVER);
        }
    }
}