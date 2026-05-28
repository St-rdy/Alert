package com.example.Alert.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String code;
    private String message;
    private T data; // 제네릭 타입

    public static <T> ApiResponse<T> success(T data, String message, int status) {
        return new ApiResponse<>(status, "SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, "SUCCESS", message, data);
    }

    public static ApiResponse<Void> error(int status, String code, String message) {
        return new ApiResponse<>(status, code, message, null);
    }
}
