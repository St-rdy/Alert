package com.example.Alert.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Notification 관련 에러
    MISSING_FIELDS(400, "MISSING_FIELDS", "필수 필드가 누락되었습니다."),
    INVALID_TYPE(400, "INVALID_TYPE", "유효하지 않은 타입입니다."),
    USER_NOT_FOUND(404, "USER_NOT_FOUND", "대상 사용자를 찾을 수없습니다."),
    DB_ERROR(500, "DB_ERROR", "데이터베이스 오류가 발생했습니다."),

    // 공통 서버 에러
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "서버에서 알 수 없는 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;

}
