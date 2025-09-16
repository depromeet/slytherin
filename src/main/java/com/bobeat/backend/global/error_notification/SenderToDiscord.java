package com.bobeat.backend.global.error_notification;

import com.bobeat.backend.global.config.DiscordWebhookConfig;
import com.bobeat.backend.global.config.ErrorNotificationConfig;
import com.bobeat.backend.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * 디스코드 웹훅을 통한 에러 알림 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SenderToDiscord implements ErrorNotificationService {

    private final DiscordWebhookConfig discordWebhookConfig;
    private final ErrorNotificationConfig errorNotificationConfig;
    private final WebClient webClient;

    @Override
    public void sendErrorNotification(ErrorCode errorCode, Exception exception, HttpServletRequest request) {
        if (!shouldSendNotification(errorCode, request)) {
            return;
        }

        try {
            ErrorNotificationDto notificationDto = ErrorNotificationDto.create(errorCode, exception, request);
            sendDiscordNotification(notificationDto);
        } catch (Exception e) {
            log.error("디스코드 에러 알림 전송 실패", e);
        }
    }

    /**
     * 알림 전송 여부를 판단합니다.
     */
    private boolean shouldSendNotification(ErrorCode errorCode, HttpServletRequest request) {
        if (!errorNotificationConfig.isDiscordNotificationEnabled()) {
            log.debug("에러 알림이 비활성화되어 있습니다.");
            return false;
        }

        if (!discordWebhookConfig.isWebhookConfigured()) {
            log.debug("디스코드 웹훅이 설정되지 않았습니다.");
            return false;
        }

        if (errorNotificationConfig.isPathExcluded(request.getRequestURI())) {
            log.debug("제외된 경로입니다: {}", request.getRequestURI());
            return false;
        }

        if (!errorNotificationConfig.isIncludeClientErrors() &&
            errorCode.getHttpStatus().is4xxClientError()) {
            log.debug("클라이언트 에러는 알림에서 제외됩니다: {}", errorCode.getCode());
            return false;
        }

        return true;
    }

    private void sendDiscordNotification(ErrorNotificationDto notificationDto) {
        try {
            DiscordErrorNotificationDto discordDto = new DiscordErrorNotificationDto(notificationDto);
            Map<String, Object> payload = Map.of("embeds", List.of(discordDto.toEmbed()));

            String payloadJson = payload.toString();
            log.debug("디스코드 페이로드 크기: {} 문자", payloadJson.length());

            webClient.post()
                    .uri(discordWebhookConfig.getWebhookUrl())
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("디스코드 에러 알림 전송 완료: {}", notificationDto.getErrorCode()))
                    .doOnError(this::logDiscordError)
                    .subscribe();
        } catch (Exception e) {
            log.error("디스코드 알림 생성 중 에러 발생", e);
        }
    }

    /**
     * 디스코드 전송 에러를 로깅합니다.
     */
    private void logDiscordError(Throwable error) {
        log.error("디스코드 웹훅 전송 실패: {}", error.getMessage());

        if (error instanceof WebClientResponseException webClientError) {
            log.error("디스코드 응답 상태: {}, 응답 본문: {}",
                    webClientError.getStatusCode(),
                    webClientError.getResponseBodyAsString());
        }
    }

    /**
     * 간단한 로그 메시지를 디스코드로 전송
     */
    public void sendLog(String title, String message) {
        sendLog(title, message, 3447003); // 기본 파란색
    }

    /**
     * 색상을 지정하여 로그 메시지를 디스코드로 전송
     */
    public void sendLog(String title, String message, int color) {
        if (!discordWebhookConfig.isWebhookConfigured()) {
            log.debug("디스코드 웹훅이 설정되지 않았습니다.");
            return;
        }

        try {
            Map<String, Object> embed = createSimpleEmbed(title, message, color);
            Map<String, Object> payload = Map.of("embeds", List.of(embed));

            webClient.post()
                    .uri(discordWebhookConfig.getWebhookUrl())
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("디스코드 로그 전송 완료: {}", title))
                    .doOnError(this::logDiscordError)
                    .subscribe();
        } catch (Exception e) {
            log.error("디스코드 로그 전송 실패", e);
        }
    }

    /**
     * 간단한 embed 객체 생성
     */
    private Map<String, Object> createSimpleEmbed(String title, String message, int color) {
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", title);
        embed.put("description", truncateMessage(message, 1000)); // 디스코드 description 제한
        embed.put("color", color);
        embed.put("timestamp", getIsoTimestamp());
        return embed;
    }

    /**
     * 메시지 길이 제한
     */
    private String truncateMessage(String message, int maxLength) {
        if (message == null) {
            return "";
        }
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength - 3) + "...";
    }

    /**
     * ISO 타임스탬프 생성
     */
    private String getIsoTimestamp() {
        return ZonedDateTime.now(java.time.ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
    }
}
