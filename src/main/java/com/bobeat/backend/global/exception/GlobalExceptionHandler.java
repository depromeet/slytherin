package com.bobeat.backend.global.exception;

import com.bobeat.backend.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleException(RuntimeException e) {
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INTERNAL_SERVER);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER.getHttpStatus())
                .body(handleException(e, errorResponse));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(handleException(e, errorResponse));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidateException(MethodArgumentNotValidException e){
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.BAD_REQUEST);
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus())
                .body(handleException(e, errorResponse));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.FORBIDDEN);
        return ResponseEntity.status(ErrorCode.FORBIDDEN.getHttpStatus())
                .body(handleException(e, errorResponse));
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleInsufficientAuthenticationException(InsufficientAuthenticationException e) {
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.UNAUTHORIZED);
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getHttpStatus())
                .body(handleException(e, errorResponse));
    }

    public ApiResponse<Object> handleException(Exception e, ErrorResponse errorResponse) {
        log.error("{}: {}", errorResponse.code(), e.getMessage());
        return ApiResponse.error(errorResponse);
    }
}
