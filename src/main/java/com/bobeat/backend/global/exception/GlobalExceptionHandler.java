package com.ourmenu.backend.global.exception;

import com.ourmenu.backend.global.response.ApiResponse;
import com.ourmenu.backend.global.response.util.ApiUtil;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleException(RuntimeException e) {
        return handleException(e, ErrorResponse.of(ErrorCode.INTERNAL_SERVER));
    }

    @ExceptionHandler(CustomException.class)
    public ApiResponse<?> handleCustomException(CustomException e) {
        return handleException(e, ErrorResponse.of(e.getMessage(), e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidateException(MethodArgumentNotValidException e){
        return handleException(e, ErrorResponse.of(ErrorCode.BAD_REQUEST));
    }

    public ApiResponse<?> handleException(Exception e, ErrorResponse errorResponse) {
        log.error("{}: {}", errorResponse.code(), e.getMessage());
        return ApiUtil.error(errorResponse);
    }
}
