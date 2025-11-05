package com.bobeat.backend.domain.security.oauth.service;

import com.bobeat.backend.domain.security.oauth.dto.AppleUserInfo;
import com.bobeat.backend.domain.security.oauth.dto.OAuth2UserInfo;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppleService implements OAuth2Service {

    @Override
    public OAuth2UserInfo getUser(String idToken) {
        try {
            String[] chunks = idToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(payload);
            return AppleUserInfo.from(jsonNode);
        } catch (Exception e) {
            log.error("구글 사용자 정보 조회 실패", e);
            throw new CustomException(ErrorCode.APPLE_TOKEN_VALIDATION_FAIL);
        }
    }
}