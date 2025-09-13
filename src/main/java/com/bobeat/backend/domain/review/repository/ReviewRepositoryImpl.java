package com.bobeat.backend.domain.review.repository;

import static com.bobeat.backend.domain.review.entity.QReview.review;

import com.bobeat.backend.domain.review.entity.Review;
import com.bobeat.backend.global.request.CursorPaginationRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Review> findByStoreIdWithCursor(Long storeId, CursorPaginationRequest request) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(review.store.id.eq(storeId));

        if (request.lastKnown() != null) {
            Long cursor = Long.parseLong(request.lastKnown());
            builder.and(review.id.lt(cursor));
        }

        return queryFactory
                .selectFrom(review)
                .leftJoin(review.member).fetchJoin()
                .where(builder)
                .orderBy(review.id.desc())
                .limit(request.limit() + 1)
                .fetch();
    }

    @Override
    public List<Review> findByMemberIdWithCursor(Long memberId, CursorPaginationRequest request) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(review.member.id.eq(memberId));

        if (request.lastKnown() != null) {
            Long cursor = Long.parseLong(request.lastKnown());
            builder.and(review.id.lt(cursor));
        }

        return queryFactory
                .selectFrom(review)
                .leftJoin(review.store).fetchJoin()
                .where(builder)
                .orderBy(review.id.desc())
                .limit(request.limit() + 1)
                .fetch();
    }
}
