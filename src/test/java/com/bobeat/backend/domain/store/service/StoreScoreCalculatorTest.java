//package com.bobeat.backend.domain.store.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//import com.bobeat.backend.domain.member.entity.Level;
//import com.bobeat.backend.domain.store.config.StoreScoringConfig;
//import com.bobeat.backend.domain.store.entity.*;
//import com.bobeat.backend.domain.store.repository.MenuRepository;
//import com.bobeat.backend.domain.store.repository.SeatOptionRepository;
//import com.bobeat.backend.domain.store.repository.StoreRepository;
//import com.bobeat.backend.domain.store.vo.Categories;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//
///**
// * StoreScoreCalculator 단위 테스트
// *
// * 테스트 범위:
// * 1. 혼밥레벨 점수 계산
// * 2. 가격 점수 계산
// * 3. 좌석 점수 계산
// * 4. 카테고리 점수 계산
// * 5. 복합 점수 계산
// * 6. 배치 업데이트 로직
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("StoreScoreCalculator 단위 테스트")
//class StoreScoreCalculatorTest {
//
//    @Mock
//    private StoreRepository storeRepository;
//
//    @Mock
//    private MenuRepository menuRepository;
//
//    @Mock
//    private SeatOptionRepository seatOptionRepository;
//
//    @Mock
//    private StoreScoringConfig scoringConfig;
//
//    @InjectMocks
//    private StoreScoreCalculator storeScoreCalculator;
//
//    private Store testStore;
//    private PrimaryCategory categoryKorean;
//    private PrimaryCategory categoryFastFood;
//
//    @BeforeEach
//    void setUp() {
//        // 기본 설정값 설정
//        when(scoringConfig.getHonbobLevelWeight()).thenReturn(30.0);
//        when(scoringConfig.getPriceWeight()).thenReturn(25.0);
//        when(scoringConfig.getSeatTypeWeight()).thenReturn(25.0);
//        when(scoringConfig.getCategoryWeight()).thenReturn(20.0);
//
//        StoreScoringConfig.PriceThreshold priceThreshold = new StoreScoringConfig.PriceThreshold();
//        priceThreshold.setLow(8000);
//        priceThreshold.setHigh(20000);
//        when(scoringConfig.getPriceThreshold()).thenReturn(priceThreshold);
//
//        // 카테고리별 가중치
//        when(scoringConfig.getCategoryWeightRatio("한식")).thenReturn(0.6);
//        when(scoringConfig.getCategoryWeightRatio("패스트푸드")).thenReturn(1.0);
//        when(scoringConfig.getCategoryWeightRatio("일식")).thenReturn(0.7);
//
//        categoryKorean = PrimaryCategory.builder().id(1L).primaryType("한식").build();
//        categoryFastFood = PrimaryCategory.builder().id(2L).primaryType("패스트푸드").build();
//
//        testStore = Store.builder()
//            .id(1L)
//            .name("Test Store")
//            .honbobLevel(Level.LEVEL_1)
//            .categories(new Categories(categoryKorean, null))
//            .build();
//    }
//
//    // ==================== 혼밥레벨 점수 테스트 ====================
//
//    @Nested
//    @DisplayName("혼밥레벨 점수 계산")
//    class HonbobLevelScoreTests {
//
//        @Test
//        @DisplayName("레벨 1 (하) - 만점")
//        void testLevel1_FullScore() {
//            // given
//            testStore = Store.builder()
//                .id(1L)
//                .honbobLevel(Level.LEVEL_1)
//                .categories(new Categories(categoryKorean, null))
//                .build();
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: honbobLevelScore = 30.0, 나머지는 기본값
//            // 30 (혼밥) + 12.5 (가격 중간) + 0 (좌석 없음) + 12 (카테고리 0.6*20) = 54.5
//            assertThat(score).isCloseTo(54.5, withinPercentage(5));
//        }
//
//        @Test
//        @DisplayName("레벨 2 (중) - 67%")
//        void testLevel2_67Percent() {
//            // given
//            testStore = Store.builder()
//                .id(1L)
//                .honbobLevel(Level.LEVEL_2)
//                .categories(new Categories(categoryKorean, null))
//                .build();
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: honbobLevelScore = 30 * 0.67 = 20.1
//            assertThat(score).isLessThan(54.5); // Level 1보다 낮아야 함
//        }
//
//        @Test
//        @DisplayName("레벨 3 (중상) - 33%")
//        void testLevel3_33Percent() {
//            // given
//            testStore = Store.builder()
//                .id(1L)
//                .honbobLevel(Level.LEVEL_3)
//                .categories(new Categories(categoryKorean, null))
//                .build();
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: honbobLevelScore = 30 * 0.33 = 9.9
//            assertThat(score).isLessThan(45.0);
//        }
//
//        @Test
//        @DisplayName("레벨 4 (상) - 0점")
//        void testLevel4_ZeroScore() {
//            // given
//            testStore = Store.builder()
//                .id(1L)
//                .honbobLevel(Level.LEVEL_4)
//                .categories(new Categories(categoryKorean, null))
//                .build();
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: honbobLevelScore = 0
//            assertThat(score).isLessThan(40.0); // 혼밥 점수 없음
//        }
//
//        @Test
//        @DisplayName("레벨 null - 기본값 (50%)")
//        void testLevelNull_DefaultScore() {
//            // given
//            testStore = Store.builder()
//                .id(1L)
//                .honbobLevel(null)
//                .categories(new Categories(categoryKorean, null))
//                .build();
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: honbobLevelScore = 30 * 0.5 = 15
//            assertThat(score).isGreaterThan(0);
//        }
//    }
//
//    // ==================== 가격 점수 테스트 ====================
//
//    @Nested
//    @DisplayName("가격 점수 계산")
//    class PriceScoreTests {
//
//        @Test
//        @DisplayName("추천 메뉴 8000원 이하 - 만점")
//        void testPrice_Below8000_FullScore() {
//            // given
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of(
//                createMenu("메뉴1", 7000, true)
//            ));
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: priceScore = 25.0
//            assertThat(score).isGreaterThan(50.0);
//        }
//
//        @Test
//        @DisplayName("추천 메뉴 20000원 이상 - 0점")
//        void testPrice_Above20000_ZeroScore() {
//            // given
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of(
//                createMenu("메뉴1", 25000, true)
//            ));
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: priceScore = 0
//            assertThat(score).isLessThan(50.0);
//        }
//
//        @Test
//        @DisplayName("추천 메뉴 중간 가격 (14000원) - 선형 감소")
//        void testPrice_Middle_LinearDecay() {
//            // given: 14000원 = (20000 - 14000) / (20000 - 8000) = 6000 / 12000 = 0.5
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of(
//                createMenu("메뉴1", 14000, true)
//            ));
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: priceScore = 25 * 0.5 = 12.5
//            // 총점 = 30 (혼밥) + 12.5 (가격) + 0 (좌석) + 12 (카테고리) = 54.5
//            assertThat(score).isCloseTo(54.5, withinPercentage(5));
//        }
//
//        @Test
//        @DisplayName("추천 메뉴 없으면 일반 메뉴 최저가 사용")
//        void testPrice_NoRecommendMenu_UseNormalMenu() {
//            // given
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of(
//                createMenu("메뉴1", 10000, false),
//                createMenu("메뉴2", 7000, false)
//            ));
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: 7000원으로 계산되어 만점
//            assertThat(score).isGreaterThan(50.0);
//        }
//
//        @Test
//        @DisplayName("메뉴 없으면 중간 점수 (기본값 15000원)")
//        void testPrice_NoMenu_DefaultScore() {
//            // given
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: priceScore = 25 / 2 = 12.5 (중간 점수)
//            assertThat(score).isGreaterThan(0);
//        }
//
//        @Test
//        @DisplayName("여러 추천 메뉴 중 최저가 선택")
//        void testPrice_MultipleRecommendMenus_UseMinPrice() {
//            // given
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of(
//                createMenu("메뉴1", 15000, true),
//                createMenu("메뉴2", 7000, true),  // 최저가
//                createMenu("메뉴3", 12000, true)
//            ));
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: 7000원으로 계산되어 만점
//            assertThat(score).isGreaterThan(50.0);
//        }
//    }
//
//    // ==================== 좌석 점수 테스트 ====================
//
//    @Nested
//    @DisplayName("좌석 점수 계산")
//    class SeatScoreTests {
//
//        @Test
//        @DisplayName("1인석 + 바테이블 둘 다 있음 - 만점")
//        void testSeat_BothForOneAndBar_FullScore() {
//            // given
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of(
//                createSeatOption(SeatType.FOR_ONE),
//                createSeatOption(SeatType.BAR_TABLE)
//            ));
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: seatScore = 25.0
//            assertThat(score).isGreaterThan(50.0);
//        }
//
//        @Test
//        @DisplayName("1인석만 있음 - 60%")
//        void testSeat_OnlyForOne_60Percent() {
//            // given
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of(
//                createSeatOption(SeatType.FOR_ONE)
//            ));
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: seatScore = 25 * 0.6 = 15
//            assertThat(score).isGreaterThan(40.0).isLessThan(60.0);
//        }
//
//        @Test
//        @DisplayName("바테이블만 있음 - 60%")
//        void testSeat_OnlyBar_60Percent() {
//            // given
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of(
//                createSeatOption(SeatType.BAR_TABLE)
//            ));
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: seatScore = 25 * 0.6 = 15
//            assertThat(score).isGreaterThan(40.0).isLessThan(60.0);
//        }
//
//        @Test
//        @DisplayName("다른 좌석만 있음 - 20%")
//        void testSeat_OtherSeats_20Percent() {
//            // given
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of(
//                createSeatOption(SeatType.FOR_TWO),
//                createSeatOption(SeatType.FOR_FOUR)
//            ));
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: seatScore = 25 * 0.2 = 5
//            assertThat(score).isGreaterThan(30.0).isLessThan(55.0);
//        }
//
//        @Test
//        @DisplayName("좌석 정보 없음 - 0점")
//        void testSeat_NoSeatInfo_ZeroScore() {
//            // given
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: seatScore = 0
//            assertThat(score).isLessThan(50.0);
//        }
//    }
//
//    // ==================== 카테고리 점수 테스트 ====================
//
//    @Nested
//    @DisplayName("카테고리 점수 계산")
//    class CategoryScoreTests {
//
//        @Test
//        @DisplayName("패스트푸드 - 만점 (ratio 1.0)")
//        void testCategory_FastFood_FullScore() {
//            // given
//            testStore = Store.builder()
//                .id(1L)
//                .honbobLevel(Level.LEVEL_1)
//                .categories(new Categories(categoryFastFood, null))
//                .build();
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: categoryScore = 20 * 1.0 = 20
//            assertThat(score).isGreaterThan(55.0);
//        }
//
//        @Test
//        @DisplayName("한식 - 60% (ratio 0.6)")
//        void testCategory_Korean_60Percent() {
//            // given (기본 testStore는 한식)
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: categoryScore = 20 * 0.6 = 12
//            assertThat(score).isGreaterThan(40.0).isLessThan(60.0);
//        }
//
//        @Test
//        @DisplayName("카테고리 null - 중간 점수 (50%)")
//        void testCategory_Null_DefaultScore() {
//            // given
//            testStore = Store.builder()
//                .id(1L)
//                .honbobLevel(Level.LEVEL_1)
//                .categories(null)
//                .build();
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of());
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: categoryScore = 20 / 2 = 10
//            assertThat(score).isGreaterThan(30.0);
//        }
//    }
//
//    // ==================== 복합 점수 테스트 ====================
//
//    @Nested
//    @DisplayName("복합 점수 계산")
//    class CompositeScoreTests {
//
//        @Test
//        @DisplayName("완벽한 매장 - 최대 점수 (100점)")
//        void testPerfectStore_MaxScore() {
//            // given: 모든 조건 만점
//            testStore = Store.builder()
//                .id(1L)
//                .honbobLevel(Level.LEVEL_1)  // 30점
//                .categories(new Categories(categoryFastFood, null))  // 20점
//                .build();
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of(
//                createMenu("메뉴1", 7000, true)  // 25점
//            ));
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of(
//                createSeatOption(SeatType.FOR_ONE),
//                createSeatOption(SeatType.BAR_TABLE)  // 25점
//            ));
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: 30 + 25 + 25 + 20 = 100
//            assertThat(score).isEqualTo(100.0);
//        }
//
//        @Test
//        @DisplayName("최악의 매장 - 최소 점수")
//        void testWorstStore_MinScore() {
//            // given
//            testStore = Store.builder()
//                .id(1L)
//                .honbobLevel(Level.LEVEL_4)  // 0점
//                .categories(null)  // 10점 (기본)
//                .build();
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of(
//                createMenu("메뉴1", 50000, true)  // 0점
//            ));
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of(
//                createSeatOption(SeatType.FOR_FOUR)  // 5점 (20%)
//            ));
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then: 0 + 0 + 5 + 10 = 15
//            assertThat(score).isLessThan(20.0);
//        }
//
//        @Test
//        @DisplayName("점수는 100점을 초과하지 않음")
//        void testScore_NotExceed100() {
//            // given: 이론적으로 100점 초과 가능한 상황 (없어야 함)
//            testStore = Store.builder()
//                .id(1L)
//                .honbobLevel(Level.LEVEL_1)
//                .categories(new Categories(categoryFastFood, null))
//                .build();
//            when(menuRepository.findByStore(testStore)).thenReturn(List.of(
//                createMenu("메뉴1", 5000, true)
//            ));
//            when(seatOptionRepository.findByStore(testStore)).thenReturn(List.of(
//                createSeatOption(SeatType.FOR_ONE),
//                createSeatOption(SeatType.BAR_TABLE)
//            ));
//
//            // when
//            double score = storeScoreCalculator.calculateStoreScore(testStore);
//
//            // then
//            assertThat(score).isLessThanOrEqualTo(100.0);
//        }
//    }
//
//    // ==================== 배치 업데이트 테스트 ====================
//
//    @Nested
//    @DisplayName("배치 업데이트 로직")
//    class BatchUpdateTests {
//
//        @Test
//        @DisplayName("업데이트 필요한 매장만 처리")
//        void testCalculateAndUpdatePendingScores() {
//            // given
//            Store store1 = Store.builder().id(1L).honbobLevel(Level.LEVEL_1)
//                .categories(new Categories(categoryKorean, null)).scoreUpdateFlag(true).build();
//            Store store2 = Store.builder().id(2L).honbobLevel(Level.LEVEL_2)
//                .categories(new Categories(categoryKorean, null)).internalScore(null).build();
//            Store store3 = Store.builder().id(3L).honbobLevel(Level.LEVEL_3)
//                .categories(new Categories(categoryKorean, null)).internalScore(50.0).scoreUpdateFlag(false).build();
//
//            when(storeRepository.findStoresNeedingScoreUpdate()).thenReturn(List.of(store1, store2));
//            when(menuRepository.findByStore(any())).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(any())).thenReturn(List.of());
//
//            // when
//            int updatedCount = storeScoreCalculator.calculateAndUpdatePendingScores();
//
//            // then
//            assertThat(updatedCount).isEqualTo(2);
//            verify(storeRepository, times(1)).saveAll(argThat(stores ->
//                stores.size() == 2 &&
//                stores.stream().allMatch(s -> s.getInternalScore() != null)
//            ));
//        }
//
//        @Test
//        @DisplayName("전체 매장 강제 재계산")
//        void testCalculateAndUpdateAllScores() {
//            // given
//            Store store1 = Store.builder().id(1L).honbobLevel(Level.LEVEL_1)
//                .categories(new Categories(categoryKorean, null)).build();
//            Store store2 = Store.builder().id(2L).honbobLevel(Level.LEVEL_2)
//                .categories(new Categories(categoryKorean, null)).build();
//
//            when(storeRepository.findAll()).thenReturn(List.of(store1, store2));
//            when(menuRepository.findByStore(any())).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(any())).thenReturn(List.of());
//
//            // when
//            int updatedCount = storeScoreCalculator.calculateAndUpdateAllScores();
//
//            // then
//            assertThat(updatedCount).isEqualTo(2);
//            verify(storeRepository, times(1)).saveAll(any());
//        }
//
//        @Test
//        @DisplayName("점수 계산 실패 시에도 다른 매장 처리 계속")
//        void testContinueOnError() {
//            // given
//            Store store1 = Store.builder().id(1L).honbobLevel(Level.LEVEL_1)
//                .categories(new Categories(categoryKorean, null)).build();
//            Store store2 = Store.builder().id(2L).honbobLevel(Level.LEVEL_2)
//                .categories(new Categories(categoryKorean, null)).build();
//
//            when(storeRepository.findStoresNeedingScoreUpdate()).thenReturn(List.of(store1, store2));
//            when(menuRepository.findByStore(store1)).thenThrow(new RuntimeException("DB Error"));
//            when(menuRepository.findByStore(store2)).thenReturn(List.of());
//            when(seatOptionRepository.findByStore(any())).thenReturn(List.of());
//
//            // when
//            int updatedCount = storeScoreCalculator.calculateAndUpdatePendingScores();
//
//            // then: store2는 성공해야 함
//            assertThat(updatedCount).isEqualTo(1);
//        }
//    }
//
//    // ==================== Helper Methods ====================
//
//    private Menu createMenu(String name, int price, boolean recommend) {
//        return Menu.builder()
//            .id(1L)
//            .store(testStore)
//            .name(name)
//            .price(price)
//            .recommend(recommend)
//            .build();
//    }
//
//    private SeatOption createSeatOption(SeatType seatType) {
//        return SeatOption.builder()
//            .id(1L)
//            .store(testStore)
//            .seatType(seatType)
//            .build();
//    }
//
//    private static org.assertj.core.data.Percentage withinPercentage(int percentage) {
//        return org.assertj.core.data.Percentage.withPercentage(percentage);
//    }
//}