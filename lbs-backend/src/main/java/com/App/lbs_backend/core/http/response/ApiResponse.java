package com.App.lbs_backend.core.http.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        String path
) {
    public static <T> ApiResponse<T> apiSuccess(String message, T data, String path) {
        return new ApiResponse<>(true, message, data, path);
    }
    
    public static <T> ApiResponse<T> apiError(String message, String path) {
        return new ApiResponse<>(false, message, null, path);
    }
}
