package com.bobeat.backend.domain.review.service;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.review.dto.request.CreateReviewRequest;
import com.bobeat.backend.domain.review.dto.request.UpdateReviewRequest;
import com.bobeat.backend.domain.review.entity.Review;
import com.bobeat.backend.domain.review.entity.ReviewKeyword;
import com.bobeat.backend.domain.review.repository.ReviewRepository;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private StoreRepository storeRepository;
    
    @InjectMocks
    private ReviewService reviewService;

    @Test
    void 이미_리뷰를_작성한_가게에_다시_리뷰_작성시_예외_발생() {
        // given
        Long memberId = 1L;
        Long storeId = 1L;
        CreateReviewRequest request = createReviewRequest(storeId);
        
        Member member = createMember(memberId);
        Store store = createStore(storeId);
        
        given(memberRepository.findByIdOrElseThrow(memberId)).willReturn(member);
        given(storeRepository.findByIdOrThrow(storeId)).willReturn(store);
        given(reviewRepository.existsByMemberIdAndStoreId(memberId, storeId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.REVIEW_ALREADY_EXISTS.getMessage());
    }

    @Test
    void 다른_사용자의_리뷰_수정시_예외_발생() {
        // given
        Long reviewOwnerId = 1L;
        Long otherMemberId = 2L;
        Long reviewId = 1L;
        
        UpdateReviewRequest request = createUpdateReviewRequest();
        Review review = createReviewWithMember(reviewId, reviewOwnerId);
        
        given(reviewRepository.findByIdOrThrow(reviewId)).willReturn(review);

        // when & then
        assertThatThrownBy(() -> reviewService.updateReview(otherMemberId, reviewId, request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.REVIEW_ACCESS_DENIED.getMessage());
    }

    @Test
    void 다른_사용자의_리뷰_삭제시_예외_발생() {
        // given
        Long reviewOwnerId = 1L;
        Long otherMemberId = 2L;
        Long reviewId = 1L;
        
        Review review = createReviewWithMember(reviewId, reviewOwnerId);
        
        given(reviewRepository.findByIdOrThrow(reviewId)).willReturn(review);

        // when & then
        assertThatThrownBy(() -> reviewService.deleteReview(otherMemberId, reviewId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.REVIEW_ACCESS_DENIED.getMessage());
    }

    @Test
    void 본인_리뷰_수정_성공() {
        // given
        Long memberId = 1L;
        Long reviewId = 1L;
        
        UpdateReviewRequest request = createUpdateReviewRequest();
        Review review = createReviewWithMember(reviewId, memberId);
        
        given(reviewRepository.findByIdOrThrow(reviewId)).willReturn(review);

        // when
        reviewService.updateReview(memberId, reviewId, request);

        // then
        verify(reviewRepository).findByIdOrThrow(reviewId);
        assertThat(review.getContent()).isEqualTo("수정된 리뷰 내용");
        assertThat(review.getKeywords()).containsExactly(ReviewKeyword.KIND_SERVICE);
    }

    @Test
    @DisplayName("본인 리뷰 삭제가 성공한다")
    void 본인_리뷰_삭제_성공() {
        // given
        Long memberId = 1L;
        Long reviewId = 1L;
        
        Review review = createReviewWithMember(reviewId, memberId);
        
        given(reviewRepository.findByIdOrThrow(reviewId)).willReturn(review);

        // when
        reviewService.deleteReview(memberId, reviewId);

        // then
        verify(reviewRepository).delete(review);
    }

    private CreateReviewRequest createReviewRequest(Long storeId) {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setStoreId(storeId);
        request.setContent("맛있어요!");
        request.setKeywords(List.of(ReviewKeyword.BEST_TASTE, ReviewKeyword.GOOD_FOR_SOLO));
        return request;
    }

    private UpdateReviewRequest createUpdateReviewRequest() {
        UpdateReviewRequest request = new UpdateReviewRequest();
        request.setContent("수정된 리뷰 내용");
        request.setKeywords(List.of(ReviewKeyword.KIND_SERVICE));
        return request;
    }

    private Member createMember(Long memberId) {
        return Member.builder()
                .id(memberId)
                .nickname("테스트유저")
                .build();
    }

    private Store createStore(Long storeId) {
        return Store.builder()
                .id(storeId)
                .name("테스트 가게")
                .build();
    }

    private Review createReviewWithMember(Long reviewId, Long memberId) {
        Member member = createMember(memberId);
        Store store = createStore(1L);
        
        return Review.builder()
                .id(reviewId)
                .content("기존 리뷰 내용")
                .keywords(List.of(ReviewKeyword.BEST_TASTE))
                .member(member)
                .store(store)
                .build();
    }
}
