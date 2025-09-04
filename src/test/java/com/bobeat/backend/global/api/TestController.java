package com.bobeat.backend.global.api;

import com.bobeat.backend.global.exception.ErrorResponse;
import com.bobeat.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test/success")
    public ApiResponse<TestApiResponse> getSuccess() {
        TestApiResponse response = new TestApiResponse("응답 값");
        return ApiResponse.success(response);
    }

    @GetMapping("/test/error")
    public ApiResponse<String> getError() {
        throw new RuntimeException("허용되지 않은 API입니다");
    }

    @GetMapping("/test/success-only")
    public ApiResponse<Void> getSuccessOnly() {
        return ApiResponse.successOnly();
    }

    @GetMapping("/test/custom-error")
    public ApiResponse<String> getCustomError() {
        throw new TestCustomException();
    }

    @GetMapping("test/custom-error/message")
    public ApiResponse<String> getCustomErrorWithMessage() {
        throw new TestCustomException("허용되지 않은 API입니다");
    }
}
