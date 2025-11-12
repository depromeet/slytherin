package com.bobeat.backend.domain.review.controller;

import com.bobeat.backend.domain.review.dto.request.CreateReviewRequest;
import com.bobeat.backend.domain.review.dto.request.UpdateReviewRequest;
import com.bobeat.backend.domain.review.dto.response.MyReviewResponse;
import com.bobeat.backend.domain.review.dto.response.ReviewResponse;
import com.bobeat.backend.domain.review.service.ReviewService;
import com.bobeat.backend.global.request.CursorPaginationRequest;
import com.bobeat.backend.global.response.ApiResponse;
import com.bobeat.backend.global.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Review", description = "리뷰 관련 API")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "특정 가게에 리뷰를 작성합니다.")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ApiResponse<ReviewResponse> createReview(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid CreateReviewRequest request
    ) {
        ReviewResponse response = reviewService.createReview(memberId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "가게별 리뷰 목록 조회", description = "특정 가게의 리뷰 목록을 커서 기반으로 조회합니다.")
    @GetMapping("/stores/{storeId}")
    public ApiResponse<CursorPageResponse<ReviewResponse>> getReviewsByStore(
            @Parameter(description = "가게 ID", example = "1") @PathVariable Long storeId,
            @ModelAttribute @Valid CursorPaginationRequest request
    ) {
        CursorPageResponse<ReviewResponse> response = reviewService.getReviewsByStore(storeId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "내 리뷰 목록 조회", description = "로그인한 사용자의 리뷰 목록을 커서 기반으로 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ApiResponse<CursorPageResponse<MyReviewResponse>> getMyReviews(
            @AuthenticationPrincipal Long memberId,
            @ModelAttribute @Valid CursorPaginationRequest request
    ) {
        CursorPageResponse<MyReviewResponse> response = reviewService.getMyReviews(memberId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "리뷰 상세 조회", description = "특정 리뷰의 상세 정보를 조회합니다.")
    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> getReviewById(
            @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId
    ) {
        ReviewResponse response = reviewService.getReviewById(reviewId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "리뷰 수정", description = "작성한 리뷰를 수정합니다.")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> updateReview(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
            @RequestBody @Valid UpdateReviewRequest request
    ) {
        ReviewResponse response = reviewService.updateReview(memberId, reviewId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "리뷰 삭제", description = "작성한 리뷰를 삭제합니다.")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(memberId, reviewId);
        return ApiResponse.successOnly();
    }
}