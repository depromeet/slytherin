package com.bobeat.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    // 전역
    INTERNAL_SERVER(HttpStatus.INTERNAL_SERVER_ERROR, "G500", "서버 내부에서 에러가 발생하였습니다"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "G400", "잘못된 요청입니다."),
    FORBIDDEN_RESOURCE(HttpStatus.FORBIDDEN, "G403", "접근할 수 없는 리소스입니다"),
    NOT_FOUND_RESOURCE(HttpStatus.NOT_FOUND, "G404", "찾을 수 없는 리소스입니다"),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다."),
    INVALID_LEVEL_VALUE(HttpStatus.BAD_REQUEST, "M002", "유효하지 않은 레벨 값입니다."),
    INVALID_ONBOARDING_QUESTION(HttpStatus.BAD_REQUEST, "M003", "유효하지 않은 온보딩 질문 또는 옵션입니다.");

    private final HttpStatus httpStatus;

    private final String code;

    private final String message;
}
