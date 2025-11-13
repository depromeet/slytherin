package com.bobeat.backend.domain.security.oauth.service;

import com.bobeat.backend.domain.security.oauth.dto.GoogleUserInfo;
import com.bobeat.backend.domain.security.oauth.dto.OAuth2UserInfo;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleService implements OAuth2Service {

    private final WebClient webClient;

    private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final String USER_UNLINK_URI = " https://oauth2.googleapis.com/revoke";

    @Override
    public OAuth2UserInfo getUser(String accessToken) {
        try {
            JsonNode response = webClient.get()
                    .uri(USER_INFO_URI)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null) {
                throw new CustomException(ErrorCode.GOOGLE_TOKEN_VALIDATION_FAIL);
            }

            return GoogleUserInfo.from(response);
        } catch (Exception e) {
            log.error("구글 사용자 정보 조회 실패", e);
            throw new CustomException(ErrorCode.GOOGLE_TOKEN_VALIDATION_FAIL);
        }
    }

    @Override
    public void unlink(String accessToken) {
        try {
            webClient.post()
                    .uri(USER_UNLINK_URI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("구글 사용자 해제 실패");
                        return Mono.error(new CustomException(ErrorCode.APPLE_UNLINK_FAIL));
                    })
                    .toBodilessEntity()
                    .block();

        } catch (Exception e) {
            log.error("구글 사용자 해제 실패", e);
            throw new CustomException(ErrorCode.APPLE_UNLINK_FAIL);
        }
    }
} 
