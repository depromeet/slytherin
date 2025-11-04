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
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "G401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "G403", "접근이 금지되었습니다."),
    FORBIDDEN_RESOURCE(HttpStatus.FORBIDDEN, "G403", "접근할 수 없는 리소스입니다"),
    NOT_FOUND_RESOURCE(HttpStatus.NOT_FOUND, "G404", "찾을 수 없는 리소스입니다"),

    //식당
    NOT_FOUND_STORE(HttpStatus.NOT_FOUND, "R404", "찾을 수 없는 식당입니다"),
    NOT_FOUND_STORE_CATEGORY(HttpStatus.NOT_FOUND, "R405", "찾을 수 없는 식당 카테고리입니다"),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다."),
    INVALID_LEVEL_VALUE(HttpStatus.BAD_REQUEST, "M002", "유효하지 않은 레벨 값입니다."),
    INVALID_ONBOARDING_QUESTION(HttpStatus.BAD_REQUEST, "M003", "유효하지 않은 온보딩 질문 또는 옵션입니다."),

    // 가게
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "가게를 찾을 수 없습니다."),

    // 리뷰
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "R002", "이미 해당 가게에 리뷰를 작성했습니다."),
    REVIEW_ACCESS_DENIED(HttpStatus.FORBIDDEN, "R003", "리뷰에 대한 접근 권한이 없습니다."),

    // OAuth & JWT
    KAKAO_TOKEN_VALIDATION_FAIL(HttpStatus.UNAUTHORIZED, "O001", "카카오 토큰 검증에 실패했습니다."),
    GOOGLE_TOKEN_VALIDATION_FAIL(HttpStatus.UNAUTHORIZED, "O002", "구글 토큰 검증에 실패했습니다."),
    PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "O003", "지원하지 않는 OAuth 프로바이더입니다."),
    JWT_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "J001", "유효하지 않은 JWT 토큰입니다."),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "J002", "만료된 JWT 토큰입니다."),
    JWT_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "J003", "지원하지 않는 JWT 토큰입니다."),
    JWT_CLAIMS_EMPTY(HttpStatus.UNAUTHORIZED, "J004", "JWT Claims가 비어있습니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "J005", "토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "J006", "리프레시 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "J007", "리프레시 토큰이 일치하지 않습니다."),
    CONCURRENCY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G501", "동시성 처리 중 오류가 발생했습니다."),

    //검색
    SEARCH_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "검색 기록을 찾을 수 없습니다."),
    SEARCH_HISTORY_ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "S002", "접근 불가한 검색 기록입니다");

    private final HttpStatus httpStatus;

    private final String code;

    private final String message;
}
