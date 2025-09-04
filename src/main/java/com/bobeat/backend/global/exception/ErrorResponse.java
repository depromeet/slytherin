package com.bobeat.backend.global.exception;

public record ErrorResponse(String code, String message) {

    public ErrorResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    public ErrorResponse(ErrorCode errorCode, String message) {
        this(errorCode.getCode(), message);
    }
}
