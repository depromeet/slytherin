package com.bobeat.backend.global.response;

import com.bobeat.backend.global.exception.ErrorResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@Getter
public class ApiResponse<T> {

    private final T response;

    private final ErrorResponse errorResponse;

    public static <T> ApiResponse<T> success(T response) {
        return new ApiResponse<>(response, null);
    }

    public static ApiResponse<?> error(ErrorResponse errorResponse) {
        return new ApiResponse<>(null, errorResponse);
    }

    public static ApiResponse<Void> successOnly() {
        return new ApiResponse<>(null, null);
    }
}
