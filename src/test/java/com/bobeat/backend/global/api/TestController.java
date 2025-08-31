package com.bobeat.backend.global.api;

import com.bobeat.backend.global.exception.ErrorResponse;
import com.bobeat.backend.global.response.ApiResponse;
import com.bobeat.backend.global.response.util.ApiUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/success")
    public ApiResponse<TestApiResponse> getSuccess() {
        TestApiResponse response = new TestApiResponse("응답 값");
        return ApiUtil.success(response);
    }

    @GetMapping("/error")
    public ApiResponse<String> getError() {
        throw new RuntimeException("허용되지 않은 API입니다");
    }

    @GetMapping("/success-only")
    public ApiResponse<Void> getSuccessOnly() {
        return ApiUtil.successOnly();
    }
}
