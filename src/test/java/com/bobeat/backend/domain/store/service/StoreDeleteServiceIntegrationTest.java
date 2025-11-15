package com.bobeat.backend.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.entity.SocialProvider;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.review.entity.Review;
import com.bobeat.backend.domain.review.entity.ReviewKeyword;
import com.bobeat.backend.domain.review.repository.ReviewRepository;
import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.PrimaryCategory;
import com.bobeat.backend.domain.store.entity.ProposalType;
import com.bobeat.backend.domain.store.entity.SeatOption;
import com.bobeat.backend.domain.store.entity.SeatType;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreEmbedding;
import com.bobeat.backend.domain.store.entity.StoreImage;
import com.bobeat.backend.domain.store.entity.StoreProposal;
import com.bobeat.backend.domain.store.repository.MenuRepository;
import com.bobeat.backend.domain.store.repository.PrimaryCategoryRepository;
import com.bobeat.backend.domain.store.repository.SeatOptionRepository;
import com.bobeat.backend.domain.store.repository.StoreEmbeddingRepository;
import com.bobeat.backend.domain.store.repository.StoreImageRepository;
import com.bobeat.backend.domain.store.repository.StoreProposalRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.domain.store.vo.Categories;
import com.bobeat.backend.global.db.PostgreSQLTestContainer;
import com.bobeat.backend.global.exception.CustomException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@PostgreSQLTestContainer
@Transactional
class StoreDeleteServiceIntegrationTest {

    @Autowired
    private StoreDeleteService storeDeleteService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreImageRepository storeImageRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private SeatOptionRepository seatOptionRepository;

    @Autowired
    private StoreEmbeddingRepository storeEmbeddingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private StoreProposalRepository storeProposalRepository;

    @Autowired
    private PrimaryCategoryRepository primaryCategoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GeometryFactory geometryFactory;

    private Store testStore;
    private Member testMember;
    private PrimaryCategory primaryCategory;

    @BeforeEach
    void setUp() {
        // PrimaryCategory 생성
        primaryCategory = primaryCategoryRepository.findByPrimaryType("한식")
                .orElseGet(() -> primaryCategoryRepository.save(
                        PrimaryCategory.builder()
                                .primaryType("한식")
                                .build()
                ));

        // Member 생성
        testMember = memberRepository.save(
                Member.builder()
                        .nickname("테스트 회원")
                        .email("test@example.com")
                        .socialProvider(SocialProvider.KAKAO)
                        .providerId("test123")
                        .build()
        );

        // Store 생성
        Point location = geometryFactory.createPoint(new Coordinate(127.0, 37.0));
        location.setSRID(4326);

        Address address = Address.builder()
                .address("서울시 강남구")
                .latitude(37.0)
                .longitude(127.0)
                .build();
        address.setLocation(location);

        Categories categories = new Categories(primaryCategory, null);

        testStore = storeRepository.save(
                Store.builder()
                        .name("테스트 가게")
                        .address(address)
                        .phoneNumber("02-1234-5678")
                        .description("테스트 설명")
                        .honbobLevel(Level.fromValue(3))
                        .categories(categories)
                        .build()
        );
    }

    @Test
    @DisplayName("Store와 연관된 모든 엔티티가 삭제된다")
    void deleteStore_WithAllRelatedEntities() {
        // given
        Long storeId = testStore.getId();

        // StoreImage 생성
        StoreImage mainImage = storeImageRepository.save(
                StoreImage.builder()
                        .store(testStore)
                        .imageUrl("https://example.com/main.jpg")
                        .isMain(true)
                        .build()
        );

        StoreImage subImage = storeImageRepository.save(
                StoreImage.builder()
                        .store(testStore)
                        .imageUrl("https://example.com/sub.jpg")
                        .isMain(false)
                        .build()
        );

        // Menu 생성
        Menu menu1 = menuRepository.save(
                Menu.builder()
                        .store(testStore)
                        .name("김치찌개")
                        .price(9000)
                        .recommend(true)
                        .imageUrl("https://example.com/kimchi.jpg")
                        .build()
        );

        Menu menu2 = menuRepository.save(
                Menu.builder()
                        .store(testStore)
                        .name("된장찌개")
                        .price(8000)
                        .recommend(false)
                        .imageUrl("https://example.com/doenjang.jpg")
                        .build()
        );

        // SeatOption 생성
        SeatOption seatOption1 = seatOptionRepository.save(
                SeatOption.builder()
                        .store(testStore)
                        .seatType(SeatType.FOR_ONE)
                        .imageUrl("https://example.com/seat1.jpg")
                        .build()
        );

        SeatOption seatOption2 = seatOptionRepository.save(
                SeatOption.builder()
                        .store(testStore)
                        .seatType(SeatType.BAR_TABLE)
                        .imageUrl("https://example.com/seat2.jpg")
                        .build()
        );

        // StoreEmbedding 생성
        float[] embedding = new float[1024];
        for (int i = 0; i < 1024; i++) {
            embedding[i] = (float) Math.random();
        }

        StoreEmbedding storeEmbedding = storeEmbeddingRepository.save(
                StoreEmbedding.builder()
                        .store(testStore)
                        .embedding(embedding)
                        .build()
        );

        // Review 생성
        Review review1 = reviewRepository.save(
                Review.builder()
                        .store(testStore)
                        .member(testMember)
                        .content("정말 맛있어요!")
                        .keywords(List.of(ReviewKeyword.GUARANTEED_TASTE, ReviewKeyword.SPACIOUS_STORE))
                        .build()
        );

        Review review2 = reviewRepository.save(
                Review.builder()
                        .store(testStore)
                        .member(testMember)
                        .content("서비스가 좋아요")
                        .keywords(List.of(ReviewKeyword.KIND_SERVICE))
                        .build()
        );

        // StoreProposal 생성
        StoreProposal proposal1 = storeProposalRepository.save(
                StoreProposal.builder()
                        .store(testStore)
                        .member(testMember)
                        .proposalType(ProposalType.STORE_CLOSED)
                        .content("가게가 폐업했습니다")
                        .build()
        );

        StoreProposal proposal2 = storeProposalRepository.save(
                StoreProposal.builder()
                        .store(testStore)
                        .member(testMember)
                        .proposalType(ProposalType.STORE_PHONE_NUMBER)
                        .content("전화번호가 잘못되었습니다")
                        .build()
        );

        // when
        storeDeleteService.deleteStore(storeId);

        // then
        // Store가 삭제되었는지 확인
        assertThat(storeRepository.findById(storeId)).isEmpty();

        // StoreImage가 삭제되었는지 확인
        assertThat(storeImageRepository.findById(mainImage.getId())).isEmpty();
        assertThat(storeImageRepository.findById(subImage.getId())).isEmpty();

        // Menu가 삭제되었는지 확인
        assertThat(menuRepository.findById(menu1.getId())).isEmpty();
        assertThat(menuRepository.findById(menu2.getId())).isEmpty();

        // SeatOption이 삭제되었는지 확인
        assertThat(seatOptionRepository.findById(seatOption1.getId())).isEmpty();
        assertThat(seatOptionRepository.findById(seatOption2.getId())).isEmpty();

        // StoreEmbedding이 삭제되었는지 확인
        assertThat(storeEmbeddingRepository.findById(storeEmbedding.getId())).isEmpty();

        // Review가 삭제되었는지 확인
        assertThat(reviewRepository.findById(review1.getId())).isEmpty();
        assertThat(reviewRepository.findById(review2.getId())).isEmpty();

        // StoreProposal이 삭제되었는지 확인
        assertThat(storeProposalRepository.findById(proposal1.getId())).isEmpty();
        assertThat(storeProposalRepository.findById(proposal2.getId())).isEmpty();
    }

    @Test
    @DisplayName("연관 엔티티가 없는 Store도 삭제된다")
    void deleteStore_WithoutRelatedEntities() {
        // given
        Long storeId = testStore.getId();

        // when
        storeDeleteService.deleteStore(storeId);

        // then
        assertThat(storeRepository.findById(storeId)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 Store 삭제 시 예외가 발생한다")
    void deleteStore_NotFound_ThrowsException() {
        // given
        Long nonExistentStoreId = 999999L;

        // when & then
        assertThatThrownBy(() -> storeDeleteService.deleteStore(nonExistentStoreId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("일부 연관 엔티티만 있어도 Store가 삭제된다")
    void deleteStore_WithPartialRelatedEntities() {
        // given
        Long storeId = testStore.getId();

        // StoreImage와 Menu만 생성
        StoreImage image = storeImageRepository.save(
                StoreImage.builder()
                        .store(testStore)
                        .imageUrl("https://example.com/image.jpg")
                        .isMain(true)
                        .build()
        );

        Menu menu = menuRepository.save(
                Menu.builder()
                        .store(testStore)
                        .name("김치찌개")
                        .price(9000)
                        .recommend(true)
                        .build()
        );

        // when
        storeDeleteService.deleteStore(storeId);

        // then
        assertThat(storeRepository.findById(storeId)).isEmpty();
        assertThat(storeImageRepository.findById(image.getId())).isEmpty();
        assertThat(menuRepository.findById(menu.getId())).isEmpty();
    }

    @Test
    @DisplayName("다른 Store의 데이터는 삭제되지 않는다")
    void deleteStore_DoesNotAffectOtherStores() {
        // given
        // 두 번째 Store 생성
        Point location2 = geometryFactory.createPoint(new Coordinate(128.0, 38.0));
        location2.setSRID(4326);

        Address address2 = Address.builder()
                .address("서울시 강북구")
                .latitude(38.0)
                .longitude(128.0)
                .build();
        address2.setLocation(location2);

        Categories categories2 = new Categories(primaryCategory, null);

        Store otherStore = storeRepository.save(
                Store.builder()
                        .name("다른 가게")
                        .address(address2)
                        .phoneNumber("02-9876-5432")
                        .description("다른 설명")
                        .honbobLevel(Level.fromValue(2))
                        .categories(categories2)
                        .build()
        );

        // 각 Store에 데이터 생성
        StoreImage testStoreImage = storeImageRepository.save(
                StoreImage.builder()
                        .store(testStore)
                        .imageUrl("https://example.com/test.jpg")
                        .isMain(true)
                        .build()
        );

        StoreImage otherStoreImage = storeImageRepository.save(
                StoreImage.builder()
                        .store(otherStore)
                        .imageUrl("https://example.com/other.jpg")
                        .isMain(true)
                        .build()
        );

        Menu testMenu = menuRepository.save(
                Menu.builder()
                        .store(testStore)
                        .name("김치찌개")
                        .price(9000)
                        .build()
        );

        Menu otherMenu = menuRepository.save(
                Menu.builder()
                        .store(otherStore)
                        .name("된장찌개")
                        .price(8000)
                        .build()
        );

        // when
        storeDeleteService.deleteStore(testStore.getId());

        // then
        // testStore와 관련 데이터는 삭제됨
        assertThat(storeRepository.findById(testStore.getId())).isEmpty();
        assertThat(storeImageRepository.findById(testStoreImage.getId())).isEmpty();
        assertThat(menuRepository.findById(testMenu.getId())).isEmpty();

        // otherStore와 관련 데이터는 유지됨
        assertThat(storeRepository.findById(otherStore.getId())).isPresent();
        assertThat(storeImageRepository.findById(otherStoreImage.getId())).isPresent();
        assertThat(menuRepository.findById(otherMenu.getId())).isPresent();
    }
}
