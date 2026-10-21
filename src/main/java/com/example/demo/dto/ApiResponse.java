package com.example.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;

    private int code;

    private String message;

    private T data;

    // 正常成功，有資料
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                200,
                "操作成功",
                data
        );
    }

    // 正常成功，自訂訊息
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                true,
                200,
                message,
                data
        );
    }

    // 正常成功，沒有資料
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(
                true,
                200,
                message,
                null
        );
    }

    // 一般失敗
    public static ApiResponse<Void> fail(int code, String message) {
        return new ApiResponse<>(
                false,
                code,
                message,
                null
        );
    }

    // 系統異常
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(
                false,
                500,
                message,
                null
        );
    }
}