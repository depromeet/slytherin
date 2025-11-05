package com.bobeat.backend.domain.store.controller;

import com.bobeat.backend.domain.store.service.StoreScoreCalculator;
import com.bobeat.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 식당 관리자용 API
 */
@Tag(name = "Store Admin", description = "가게(식당) 관리자 API")
@RestController
@RequestMapping("/api/v1/admin/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreAdminController {

    private final StoreScoreCalculator storeScoreCalculator;

    @Operation(
        summary = "전체 식당 점수 재계산",
        description = "모든 식당의 내부 정렬 점수를 강제로 재계산합니다. " +
                     "내부 정렬 로직이 변경되었을 때 사용합니다. " +
                     "기존 점수 여부와 관계없이 모든 식당을 재계산합니다."
    )
    @PostMapping("/scores/recalculate")
    public ApiResponse<RecalculateResponse> recalculateAllScores() {
        log.info("Admin API: Starting full recalculation of all store scores");

        int updatedCount = storeScoreCalculator.calculateAndUpdateAllScores();
        log.info("Admin API: Successfully recalculated {} store scores", updatedCount);

        return ApiResponse.success(new RecalculateResponse(
            "전체 식당 점수 재계산이 완료되었습니다.",
            updatedCount
        ));
    }

    @Operation(
        summary = "대기 중인 식당 점수 계산",
        description = "점수가 없거나 업데이트 플래그가 켜진 식당들만 계산합니다. " +
                     "증분 업데이트 방식으로 빠르게 처리됩니다."
    )
    @PostMapping("/scores/calculate-pending")
    public ApiResponse<RecalculateResponse> calculatePendingScores() {
        log.info("Admin API: Starting incremental score calculation for pending stores");

        int updatedCount = storeScoreCalculator.calculateAndUpdatePendingScores();
        log.info("Admin API: Successfully calculated {} pending store scores", updatedCount);

        return ApiResponse.success(new RecalculateResponse(
            "대기 중인 식당 점수 계산이 완료되었습니다.",
            updatedCount
        ));
    }

    /**
     * 점수 재계산 결과 응답 DTO
     *
     * 개선사항:
     * - success 플래그 제거: HTTP 상태 코드로 성공/실패 구분
     * - 에러는 GlobalExceptionHandler에서 HTTP 500으로 자동 처리
     * - REST API 표준 준수
     */
    public record RecalculateResponse(
        String message,
        int updatedCount
    ) {}
}