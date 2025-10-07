package com.bobeat.backend.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.review.entity.Review;
import com.bobeat.backend.domain.review.entity.ReviewKeyword;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.global.db.PostgreSQLTestContainer;
import com.bobeat.backend.global.request.CursorPaginationRequest;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@PostgreSQLTestContainer
class ReviewRepositoryImplTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void 커서_기반_가게별_리뷰_조회_정상_동작() {
        // given
        Member member = createAndSaveMember("테스트유저");
        Store store = createAndSaveStore("테스트 가게");

        Review review1 = createAndSaveReview("첫 번째 리뷰", member, store);
        Review review2 = createAndSaveReview("두 번째 리뷰", member, store);
        Review review3 = createAndSaveReview("세 번째 리뷰", member, store);

        entityManager.flush();
        entityManager.clear();

        // when - limit 2로 조회 (실제로는 3개 조회하여 hasNext 확인)
        CursorPaginationRequest request = new CursorPaginationRequest(2, null);
        List<Review> result = reviewRepository.findByStoreIdWithCursor(store.getId(), request);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(review3.getId());
        assertThat(result.get(1).getId()).isEqualTo(review2.getId());
        assertThat(result.get(2).getId()).isEqualTo(review1.getId());
    }

    @Test
    void 커서를_사용한_다음_페이지_조회_정상_동작() {
        // given
        Member member = createAndSaveMember("테스트유저");
        Store store = createAndSaveStore("테스트 가게");

        Review review1 = createAndSaveReview("첫 번째 리뷰", member, store);
        Review review2 = createAndSaveReview("두 번째 리뷰", member, store);
        Review review3 = createAndSaveReview("세 번째 리뷰", member, store);

        entityManager.flush();
        entityManager.clear();

        // when - 커서를 review2 ID로 설정하여 그 이전 리뷰들 조회
        CursorPaginationRequest request = new CursorPaginationRequest(2, review2.getId().toString());
        List<Review> result = reviewRepository.findByStoreIdWithCursor(store.getId(), request);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(review1.getId());
    }

    @Test
    void 커서_기반_회원별_리뷰_조회_정상_동작() {
        // given
        Member member1 = createAndSaveMember("회원1");
        Member member2 = createAndSaveMember("회원2");
        Store store1 = createAndSaveStore("가게1");
        Store store2 = createAndSaveStore("가게2");

        Review member1Review1 = createAndSaveReview("회원1의 첫 리뷰", member1, store1);
        Review member1Review2 = createAndSaveReview("회원1의 둘째 리뷰", member1, store2);
        createAndSaveReview("회원2의 리뷰", member2, store1);

        entityManager.flush();
        entityManager.clear();

        // when
        CursorPaginationRequest request = new CursorPaginationRequest(5, null);
        List<Review> result = reviewRepository.findByMemberIdWithCursor(member1.getId(), request);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Review::getId)
                .containsExactly(member1Review2.getId(), member1Review1.getId());
    }

    private Member createAndSaveMember(String nickname) {
        Member member = Member.builder()
                .nickname(nickname)
                .build();
        entityManager.persist(member);
        return member;
    }

    private Store createAndSaveStore(String name) {
        Store store = Store.builder()
                .name(name)
                .build();
        entityManager.persist(store);
        return store;
    }

    private Review createAndSaveReview(String content, Member member, Store store) {
        Review review = Review.builder()
                .content(content)
                .keywords(List.of(ReviewKeyword.GUARANTEED_TASTE))
                .member(member)
                .store(store)
                .build();
        entityManager.persist(review);
        return review;
    }
}

