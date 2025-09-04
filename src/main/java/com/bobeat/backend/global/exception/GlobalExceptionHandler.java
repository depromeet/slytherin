package com.bobeat.backend.global.exception;

import com.bobeat.backend.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleException(RuntimeException e) {
        return handleException(e, new ErrorResponse(ErrorCode.INTERNAL_SERVER));
    }

    @ExceptionHandler(CustomException.class)
    public ApiResponse<?> handleCustomException(CustomException e) {
        return handleException(e, new ErrorResponse(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidateException(MethodArgumentNotValidException e){
        return handleException(e, new ErrorResponse(ErrorCode.BAD_REQUEST));
    }

    public ApiResponse<?> handleException(Exception e, ErrorResponse errorResponse) {
        log.error("{}: {}", errorResponse.code(), e.getMessage());
        return ApiResponse.error(errorResponse);
    }
}
