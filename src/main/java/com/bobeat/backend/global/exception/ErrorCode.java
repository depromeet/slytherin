package com.ourmenu.backend.global.exception;

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

    // 유저
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "U404", "해당 유저가 존재하지 않습니다."),
    NOT_MATCH_PASSWORD(HttpStatus.UNAUTHORIZED, "U401", "비밀번호가 일치하지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U409", "이미 존재하는 이메일입니다."),
    NOT_MATCH_TOKEN(HttpStatus.UNAUTHORIZED, "U401", "유저의 토큰값과 일치하지 않습니다."),
    INVALID_MEAL_TIME_COUNT(HttpStatus.BAD_REQUEST, "U400", "식사시간 배열의 크기가 유효하지 않습니다."),
    CONFIRM_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "U404", "인증 코드가 존재하지 않습니다."),
    NOT_MATCH_CONFIRM_CODE(HttpStatus.UNAUTHORIZED, "U401", "인증 코드가 일치하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "U401", "JWT 토큰의 유효 기간이 만료되었습니다."),
    SEND_CODE_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "U500", "이메일 인증 코드 전송에 실패하였습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "U401", "유효하지 않은 토큰입니다."),
    UNSUPPORTED_SIGN_IN_TYPE(HttpStatus.BAD_REQUEST, "U400", "지원하지 않는 방식입니다."),


    // 메뉴판
    FORBIDDEN_MENU_FOLDER(HttpStatus.FORBIDDEN, "F403", "소유하고 있는 메뉴판이 아닙니다"),
    NOT_FOUND_MENU_FOLDER(HttpStatus.NOT_FOUND, "F404", "찾을 수 없는 메뉴판 입니다"),
    OUT_OF_BOUND_CUSTOM_INDEX(HttpStatus.INTERNAL_SERVER_ERROR, "F500", "현재 메뉴판이 가지고 있는 최대 인덱스를 벗어납니다"),

    // 메뉴
    FORBIDDEN_MENU(HttpStatus.FORBIDDEN, "M403", "소유하고 있는 메뉴가 아닙니다"),
    NOT_FOUND_MENU(HttpStatus.NOT_FOUND, "M404", "찾을 수 없는 메뉴 입니다"),

    // S3
    UPLOAD_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "A500", "파일 업로드중 문제가 발생하였습니다"),
    DELETE_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "A500", "파일 삭제중 문제가 발생하였습니다"),

    // kakao OpenAPI
    EXCEEDED_DAILY_QUOTA(HttpStatus.INTERNAL_SERVER_ERROR, "K500", "카카오맵에서 찾을 수 없습니다"),
    NOT_FOUND_STORE_IN_KAKAO(HttpStatus.INTERNAL_SERVER_ERROR, "K500", "카카오맵에서 찾을 수 없습니다"),

    // 지도
    NOT_FOUND_MAP(HttpStatus.NOT_FOUND, "P404", "찾을 수 없는 위치 정보입니다."),

    // 홈
    INVALID_ANSWER(HttpStatus.UNAUTHORIZED, "H401", "질문과 관련 없는 대답입니다"),
    NOT_FOUND_QUESTION(HttpStatus.NOT_FOUND, "H404", "아직 질문을 생성하지 않았습니다"),
    RECREATE_QUESTION(HttpStatus.BAD_REQUEST, "H400", "질문 및 응답을 갱신해야합니다"),
    DESERIALIZE_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "H500", "역직렬화를 실패하였습니다");

    private final HttpStatus httpStatus;

    private final String code;

    private final String message;
}
