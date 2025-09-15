package com.bobeat.backend.global.exception;

import com.bobeat.backend.global.error_notification.ErrorNotificationService;
import com.bobeat.backend.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorNotificationService errorNotificationService;

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleException(RuntimeException e, HttpServletRequest request) {
        log.error("[서버 에러] from {} api", request.getRequestURI(), e);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INTERNAL_SERVER);
        sendErrorNotification(ErrorCode.INTERNAL_SERVER, e, request);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER.getHttpStatus())
                .body(handleException(e, errorResponse));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException e, HttpServletRequest request) {
        log.error("[커스텀 에러] {} from {} api", e.getErrorCode().getCode(), request.getRequestURI(), e);
        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getMessage());
        sendErrorNotification(e.getErrorCode(), e, request);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(handleException(e, errorResponse));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidateException(MethodArgumentNotValidException e, HttpServletRequest request){
        log.error("[검증 에러] from {} api", request.getRequestURI(), e);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.BAD_REQUEST);
        sendErrorNotification(ErrorCode.BAD_REQUEST, e, request);
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus())
                .body(handleException(e, errorResponse));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.error("[접근 거부] from {} api", request.getRequestURI(), e);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.FORBIDDEN);
        sendErrorNotification(ErrorCode.FORBIDDEN, e, request);
        return ResponseEntity.status(ErrorCode.FORBIDDEN.getHttpStatus())
                .body(handleException(e, errorResponse));
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleInsufficientAuthenticationException(InsufficientAuthenticationException e, HttpServletRequest request) {
        log.error("[인증 부족] from {} api", request.getRequestURI(), e);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.UNAUTHORIZED);
        sendErrorNotification(ErrorCode.UNAUTHORIZED, e, request);
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getHttpStatus())
                .body(handleException(e, errorResponse));
    }

    public ApiResponse<Object> handleException(Exception e, ErrorResponse errorResponse) {
        log.error("{}: {}", errorResponse.code(), e.getMessage());
        return ApiResponse.error(errorResponse);
    }

    /**
     * 에러 알림을 전송합니다.
     */
    private void sendErrorNotification(ErrorCode errorCode, Exception exception, HttpServletRequest request) {
        try {
            errorNotificationService.sendErrorNotification(errorCode, exception, request);
        } catch (Exception e) {
            log.error("에러 알림 전송 중 예외 발생", e);
        }
    }
}
