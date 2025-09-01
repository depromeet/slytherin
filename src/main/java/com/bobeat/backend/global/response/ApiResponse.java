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

    private final boolean success;

    private final T response;

    private final ErrorResponse errorResponse;
}
