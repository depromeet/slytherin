package com.bobeat.backend.domain.onboarding.controller;

import com.bobeat.backend.domain.onboarding.dto.response.OnBoardingResult;
import com.bobeat.backend.domain.onboarding.dto.request.OnboardingRequest;
import com.bobeat.backend.domain.onboarding.service.OnboardingService;
import com.bobeat.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Onboarding", description = "온보딩 관련 API")
@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Operation(summary = "온보딩 결과 등록", description = "온보딩 결과인 혼밥 레벨을 등록합니다.")
    @PostMapping
    public ApiResponse<OnBoardingResult> submitOnboarding(
            @Parameter(hidden = true) /*@AuthenticationPrincipal*/ Long memberId,
            @RequestBody OnboardingRequest request
    ) {
        OnBoardingResult result = onboardingService.submitOnboarding(memberId, request);
        return ApiResponse.success(result);
    }
}
