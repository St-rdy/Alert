package com.example.Alert.common.exception;

import com.example.Alert.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.error(e.getStatus(), e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>>
    handleException(Exception e) {
        log.error("Unexpected error", e);
        ErrorCode error = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(error.getStatus())
                .body(ApiResponse.error(error.getStatus(),
                        error.getCode(), error.getMessage()));
    }
}
