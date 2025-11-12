package com.bobeat.backend.domain.review.service;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.review.dto.request.CreateReviewRequest;
import com.bobeat.backend.domain.review.dto.request.UpdateReviewRequest;
import com.bobeat.backend.domain.review.dto.response.ReviewResponse;
import com.bobeat.backend.domain.review.dto.response.StoreInfo;
import com.bobeat.backend.domain.review.entity.Review;
import com.bobeat.backend.domain.review.repository.ReviewRepository;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import com.bobeat.backend.domain.store.repository.StoreImageRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import com.bobeat.backend.global.request.CursorPaginationRequest;
import com.bobeat.backend.global.response.CursorPageResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;

    @Transactional
    public ReviewResponse createReview(Long memberId, CreateReviewRequest request) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        Store store = storeRepository.findByIdOrThrow(request.getStoreId());

        if (reviewRepository.existsByMemberIdAndStoreId(memberId, request.getStoreId())) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .content(request.getContent())
                .member(member)
                .store(store)
                .keywords(request.getKeywords())
                .build();

        Review savedReview = reviewRepository.save(review);
        return ReviewResponse.from(savedReview);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<ReviewResponse> getReviewsByStore(Long storeId, CursorPaginationRequest request) {
        Store store = storeRepository.findByIdOrThrow(storeId);
        StoreImage mainImage = storeImageRepository.findByStoreAndIsMainTrue(store);

        List<Review> reviews = reviewRepository.findByStoreIdWithCursor(storeId, request);

        StoreInfo metadata = new StoreInfo(
                store.getId(),
                store.getName(),
                mainImage.getImageUrl()
        );

        return CursorPageResponse.of(
                reviews.stream().map(ReviewResponse::from).toList(),
                request.limit(),
                review -> review.getId().toString(),
                metadata
        );
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<ReviewResponse> getMyReviews(Long memberId, CursorPaginationRequest request) {
        memberRepository.findByIdOrElseThrow(memberId);

        List<Review> reviews = reviewRepository.findByMemberIdWithCursor(memberId, request);

        return CursorPageResponse.of(
                reviews.stream().map(ReviewResponse::from).toList(),
                request.limit(),
                ReviewResponse -> ReviewResponse.getId().toString());
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewRepository.findByIdOrThrow(reviewId);
        return ReviewResponse.from(review);
    }

    @Transactional
    public ReviewResponse updateReview(Long memberId, Long reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findByIdOrThrow(reviewId);

        if (!review.isOwnedBy(memberId)) {
            throw new CustomException(ErrorCode.REVIEW_ACCESS_DENIED);
        }

        review.update(request.getContent(), request.getKeywords());
        return ReviewResponse.from(review);
    }

    @Transactional
    public void deleteReview(Long memberId, Long reviewId) {
        Review review = reviewRepository.findByIdOrThrow(reviewId);

        if (!review.isOwnedBy(memberId)) {
            throw new CustomException(ErrorCode.REVIEW_ACCESS_DENIED);
        }

        reviewRepository.delete(review);
    }
}
