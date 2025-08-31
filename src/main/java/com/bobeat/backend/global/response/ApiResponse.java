package com.ourmenu.backend.global.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ourmenu.backend.global.exception.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties({"success"})
@Getter
public class ApiResponse<T> {

    @JsonProperty(value = "isSuccess")
    private final boolean isSuccess;

    @JsonProperty(value = "response")
    private final T response;

    @JsonProperty(value = "errorResponse")
    private final ErrorResponse errorResponse;
}
