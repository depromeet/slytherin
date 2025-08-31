package com.bobeat.backend.global.response.util;


import com.bobeat.backend.global.exception.ErrorResponse;
import com.bobeat.backend.global.response.ApiResponse;

public class ApiUtil {

    private ApiUtil() {
    }

    public static <T> ApiResponse<T> success(T response) {
        return new ApiResponse<>(true, response, null);
    }

    public static ApiResponse<?> error(ErrorResponse errorResponse) {
        return new ApiResponse<>(false, null, errorResponse);
    }

    public static ApiResponse<Void> successOnly() {
        return new ApiResponse<>(true, null, null);
    }
}
