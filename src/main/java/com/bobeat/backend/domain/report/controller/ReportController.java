package com.bobeat.backend.domain.report.controller;

import com.bobeat.backend.domain.report.dto.request.StoreReportRequest;
import com.bobeat.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report", description = "식당 제보 관련 API")
@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    @Operation(summary = "식당 제보 등록", description = "사용자가 새로운 식당을 제보합니다.")
    @PostMapping
    public ApiResponse<Void> reportStore(@RequestBody StoreReportRequest request) {
        return ApiResponse.successOnly();
    }
}
