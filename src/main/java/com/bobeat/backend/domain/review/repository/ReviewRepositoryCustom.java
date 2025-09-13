package com.bobeat.backend.domain.review.repository;

import com.bobeat.backend.domain.review.entity.Review;
import com.bobeat.backend.global.request.CursorPaginationRequest;

import java.util.List;

public interface ReviewRepositoryCustom {
    
    List<Review> findByStoreIdWithCursor(Long storeId, CursorPaginationRequest request);
    
    List<Review> findByMemberIdWithCursor(Long memberId, CursorPaginationRequest request);
}
