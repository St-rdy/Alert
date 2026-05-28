package com.example.Alert.common.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final int status;
    private final String code;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.status = errorCode.getStatus();
        this.code = errorCode.getCode();;
    }

}
