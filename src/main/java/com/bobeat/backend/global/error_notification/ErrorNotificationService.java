package com.bobeat.backend.global.error_notification;

import com.bobeat.backend.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 에러 알림 서비스 인터페이스 다양한 알림 채널(디스코드, 슬랙 등)을 지원할 수 있도록 확장 가능한 구조
 */
public interface ErrorNotificationService {

    /**
     * 에러 알림을 전송합니다.
     *
     * @param errorCode 에러 코드
     * @param exception 발생한 예외
     * @param request   HTTP 요청 정보
     */
    void sendErrorNotification(ErrorCode errorCode, Exception exception, HttpServletRequest request);
} 
