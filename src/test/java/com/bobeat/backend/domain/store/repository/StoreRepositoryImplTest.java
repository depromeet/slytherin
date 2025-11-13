package com.bobeat.backend.domain.store.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.entity.*;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.domain.store.vo.Categories;
import com.bobeat.backend.global.db.PostgreSQLTestContainer;
import com.bobeat.backend.global.request.CursorPaginationRequest;
import com.bobeat.backend.global.util.KeysetCursor;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * StoreRepositoryImpl 통합 테스트
 *
 * 테스트 범위:
 * 1. 필터링 (혼밥레벨, 카테고리, 가격, 좌석, 위치)
 * 2. 정렬 (거리순, 추천순)
 * 3. 페이지네이션 (커서 기반)
 * 4. 경계값 테스트
 * 5. 복합 조건 테스트
 *
 * 주요 변경사항:
 * - 혼밥레벨 필터: honbobLevel == N (정확히 일치하는 경우만 반환)
 * - 거리 필터: 기본 5km 반경 (Store D는 5.5km로 항상 제외됨)
 */
@SpringBootTest
@Transactional
@PostgreSQLTestContainer
@DisplayName("StoreRepositoryImpl 통합 테스트")
public class StoreRepositoryImplTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreRepositoryImpl storeRepositoryImpl;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private SeatOptionRepository seatOptionRepository;

    @Autowired
    private PrimaryCategoryRepository primaryCategoryRepository;

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    // 강남역 좌표 (37.4979, 127.0276)
    private static final double CENTER_LAT = 37.4979;
    private static final double CENTER_LON = 127.0276;

    private Store storeA; // 강남역에서 ~417m
    private Store storeB; // 강남역에서 ~2579m
    private Store storeC; // 강남역에서 ~2543m
    private Store storeD; // 강남역에서 ~5.5km (범위 밖)
    private Store storeE; // 강남역에서 ~2743m

    private PrimaryCategory categoryKorean;
    private PrimaryCategory categoryJapanese;
    private PrimaryCategory categoryFastFood;

    @BeforeEach
    void setUp() {
        // 카테고리 생성
        categoryKorean = primaryCategoryRepository.save(
            PrimaryCategory.builder().primaryType("한식").build()
        );
        categoryJapanese = primaryCategoryRepository.save(
            PrimaryCategory.builder().primaryType("일식").build()
        );
        categoryFastFood = primaryCategoryRepository.save(
            PrimaryCategory.builder().primaryType("패스트푸드").build()
        );

        // Store A: 강남역 근처 (~417m), 한식, 혼밥레벨 1, 저렴, 1인석 O
        storeA = createStore(
            "Store A",
            37.5013, 127.0296,  // ~417m
            Level.LEVEL_1,
            categoryKorean,
            50.0
        );
        createMenu(storeA, "김치찌개", 7000, true);
        createMenu(storeA, "된장찌개", 7500, false);
        createSeatOption(storeA, SeatType.FOR_ONE);
        createSeatOption(storeA, SeatType.FOR_TWO);

        // Store B: 강남역에서 ~2579m, 일식, 혼밥레벨 2, 중간 가격, 바테이블 O
        storeB = createStore(
            "Store B",
            37.5065, 127.0547,  // ~2579m
            Level.LEVEL_2,
            categoryJapanese,
            70.0
        );
        createMenu(storeB, "초밥세트", 12000, true);
        createMenu(storeB, "우동", 9000, false);
        createSeatOption(storeB, SeatType.BAR_TABLE);
        createSeatOption(storeB, SeatType.FOR_TWO);

        // Store C: 강남역에서 ~2543m, 패스트푸드, 혼밥레벨 1, 저렴, 1인석 O
        storeC = createStore(
            "Store C",
            37.5200, 127.0200,  // ~2543m
            Level.LEVEL_1,
            categoryFastFood,
            90.0
        );
        createMenu(storeC, "버거세트", 6000, true);
        createMenu(storeC, "치킨너겟", 5000, false);
        createSeatOption(storeC, SeatType.FOR_ONE);
        createSeatOption(storeC, SeatType.CUBICLE);

        // Store D: 강남역에서 ~5.5km (범위 밖), 한식, 혼밥레벨 3
        storeD = createStore(
            "Store D",
            37.4800, 127.0850,  // ~5.5km
            Level.LEVEL_3,
            categoryKorean,
            60.0
        );
        createMenu(storeD, "비빔밥", 10000, true);
        createSeatOption(storeD, SeatType.FOR_FOUR);

        // Store E: 강남역에서 ~2743m, 일식, 혼밥레벨 4, 비쌈
        storeE = createStore(
            "Store E",
            37.5150, 127.0500,  // ~2743m
            Level.LEVEL_4,
            categoryJapanese,
            40.0
        );
        createMenu(storeE, "오마카세", 50000, true);
        createSeatOption(storeE, SeatType.FOR_FOUR);
    }

    // ==================== 필터링 테스트 ====================

    @Nested
    @DisplayName("필터링 테스트")
    class FilteringTests {

        @Test
        @DisplayName("위치 필터 - 반경 5km 내 매장만 조회 (혼밥레벨 필터 없음)")
        void testLocationFilter_Radius() {
            // given: 혼밥레벨 필터 없이 거리만 필터
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, List.of(), null, null),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: 5km 이내인 A, B, C, E만 (D는 5.5km로 제외)
            assertThat(result).hasSize(4);
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactlyInAnyOrder("Store A", "Store B", "Store C", "Store E")
                .doesNotContain("Store D");
        }

        @Test
        @DisplayName("위치 필터 - BoundingBox 내 매장만 조회")
        void testLocationFilter_BBox() {
            // given: A, B만 포함하는 좁은 BBox (혼밥레벨 필터 없음)
            // Store A: (37.5013, 127.0296), Store B: (37.5065, 127.0547)
            StoreFilteringRequest.BoundingBox bbox = new StoreFilteringRequest.BoundingBox(
                new StoreFilteringRequest.Coordinate(37.508, 127.02),   // NW
                new StoreFilteringRequest.Coordinate(37.49, 127.055)    // SE
            );
            StoreFilteringRequest request = createRequest(
                bbox,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, null, null, null),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: BBox 내 Store A, Store B만 포함
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactlyInAnyOrder("Store A", "Store B");
        }

        @Test
        @DisplayName("혼밥레벨 필터 - Level 1 매장만 조회 (정확히 일치)")
        void testHonbobLevelFilter_Level1() {
            // given: honbobLevels == [1]
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, List.of(1), null, null),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Level 1인 Store A, Store C만 (5km 이내)
            assertThat(result)
                .extracting(row -> row.store().getHonbobLevel())
                .allMatch(level -> level == Level.LEVEL_1);
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactlyInAnyOrder("Store A", "Store C");
        }

        @Test
        @DisplayName("혼밥레벨 필터 - Level 2 매장만 조회")
        void testHonbobLevelFilter_Level2() {
            // given: honbobLevels == [2]
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, List.of(2), null, null),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Level 2인 Store B만
            assertThat(result).hasSize(1);
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactly("Store B");
        }

        @Test
        @DisplayName("혼밥레벨 필터 - Level 4 매장만 조회")
        void testHonbobLevelFilter_Level4() {
            // given: honbobLevels == [4]
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, List.of(4), null, null),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Level 4인 Store E만
            assertThat(result).hasSize(1);
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactly("Store E");
        }

        @Test
        @DisplayName("혼밥레벨 필터 - 다중 레벨 [1, 2] 매장 조회")
        void testHonbobLevelFilter_MultiLevel_1And2() {
            // given: honbobLevels == [1, 2]
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, List.of(1, 2), null, null),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Level 1 (Store A, Store C) 또는 Level 2 (Store B)
            assertThat(result).hasSize(3);
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactlyInAnyOrder("Store A", "Store B", "Store C");
        }

        @Test
        @DisplayName("혼밥레벨 필터 - 다중 레벨 [2, 4] 매장 조회")
        void testHonbobLevelFilter_MultiLevel_2And4() {
            // given: honbobLevels == [2, 4]
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, List.of(2, 4), null, null),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Level 2 (Store B) 또는 Level 4 (Store E)
            assertThat(result).hasSize(2);
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactlyInAnyOrder("Store B", "Store E");
        }

        @Test
        @DisplayName("카테고리 필터 - 한식/일식만 조회 (혼밥레벨 필터 없음)")
        void testCategoryFilter() {
            // given
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, null, null, List.of("한식", "일식")),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: 한식(A), 일식(B, E) - 5km 이내만
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactlyInAnyOrder("Store A", "Store B", "Store E");
        }

        @Test
        @DisplayName("가격 필터 - 추천 메뉴 8000~15000원 매장만 조회")
        void testPriceFilter() {
            // given: 혼밥레벨 필터 없음
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(
                    new StoreFilteringRequest.PriceRange(8000, 15000),
                    null,
                    null,
                    null
                ),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Store B(12000원)만 - Store D(10000원)는 5km 밖이므로 제외
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactly("Store B");
        }

        @Test
        @DisplayName("가격 필터 - min만 지정 (8000원 이상)")
        void testPriceFilter_MinOnly() {
            // given: 혼밥레벨 필터 없음
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(
                    new StoreFilteringRequest.PriceRange(8000, null),
                    null,
                    null,
                    null
                ),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Store B(12000원), Store E(50000원) - Store D는 5km 밖
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactlyInAnyOrder("Store B", "Store E");
        }

        @Test
        @DisplayName("가격 필터 - max만 지정 (10000원 이하)")
        void testPriceFilter_MaxOnly() {
            // given
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(
                    new StoreFilteringRequest.PriceRange(null, 10000),
                    null,
                    null,
                    null
                ),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Store A(7000원), Store C(6000원)
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactlyInAnyOrder("Store A", "Store C", "Store B");
        }

        @Test
        @DisplayName("좌석 타입 필터 - 1인석 있는 매장만 조회")
        void testSeatTypeFilter_ForOne() {
            // given: 혼밥레벨 필터 없음
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, null, List.of(SeatType.FOR_ONE), null),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Store A, Store C (5km 이내)
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactlyInAnyOrder("Store A", "Store C");
        }

        @Test
        @DisplayName("좌석 타입 필터 - 바테이블 있는 매장만 조회")
        void testSeatTypeFilter_BarTable() {
            // given
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, null, List.of(SeatType.BAR_TABLE), null),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Store B만
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactly("Store B");
        }

        @Test
        @DisplayName("복합 필터 - 혼밥레벨 1 + 1인석 + 한식/패스트푸드")
        void testComplexFilter_Level1_ForOne_Category() {
            // given: Level 1 + 1인석 + 한식/패스트푸드
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(
                    null,
                    List.of(1),
                    List.of(SeatType.FOR_ONE),
                    List.of("한식", "패스트푸드")
                ),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Store A(Level1, 한식, 1인석), Store C(Level1, 패스트푸드, 1인석)
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactlyInAnyOrder("Store A", "Store C");
        }

        @Test
        @DisplayName("복합 필터 - 혼밥레벨 2 + 가격 10000~15000원")
        void testComplexFilter_Level2_Price() {
            // given: Level 2 + 가격 10000~15000원
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(
                    new StoreFilteringRequest.PriceRange(10000, 15000),
                    List.of(2),
                    null,
                    null
                ),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Store B(Level2, 12000원)만
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactly("Store B");
        }
    }

    // ==================== 정렬 테스트 ====================

    @Nested
    @DisplayName("정렬 테스트")
    class SortingTests {

        @Test
        @DisplayName("거리순 정렬 - 가까운 순서대로 조회 (혼밥레벨 필터 없음)")
        void testDistanceSort() {
            // given: 혼밥레벨 필터 없음
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, null, null, null),
                null,
                StoreFilteringRequest.SortBy.DISTANCE
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // DEBUG: 실제 거리 출력
            System.out.println("=== 거리순 정렬 결과 ===");
            result.forEach(row -> System.out.printf("%s: %dm%n", row.store().getName(), row.distance()));

            // then: 실제 거리 순 A < C < B < E
            assertThat(result).hasSize(4); // D는 5km 밖
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactly("Store A", "Store C", "Store B", "Store E");

            // 거리가 오름차순인지 확인
            for (int i = 0; i < result.size() - 1; i++) {
                assertThat(result.get(i).distance())
                    .isLessThanOrEqualTo(result.get(i + 1).distance());
            }
        }

        @Test
        @DisplayName("추천순 정렬 - 복합 점수 기준 (혼밥레벨 필터 없음)")
        void testRecommendedSort() {
            // given: 혼밥레벨 필터 없음
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, null, null, null),
                null,
                StoreFilteringRequest.SortBy.RECOMMENDED
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: 복합 점수 = (internalScore * 0.3) + ((5000-distance)/5000*100 * 0.7)
            // Store A: (50*0.3) + ((5000-417)/5000*100*0.7) ≈ 15 + 64.2 = 79.2 (가장 높음)
            // Store B: (70*0.3) + ((5000-2079)/5000*100*0.7) ≈ 21 + 40.9 = 61.9
            // Store E: (40*0.3) + ((5000-2743)/5000*100*0.7) ≈ 12 + 31.6 = 43.6
            // Store C: (90*0.3) + ((5000-4543)/5000*100*0.7) ≈ 27 + 6.4 = 33.4

            assertThat(result).hasSize(4);
            assertThat(result)
                .extracting(row -> row.store().getName())
                .startsWith("Store A"); // 거리가 가까워서 가장 높은 점수
        }

        @Test
        @DisplayName("정렬 기본값 - sortBy null이면 거리순")
        void testDefaultSort() {
            // given: sortBy = null, 혼밥레벨 필터 없음
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, null, null, null),
                null,
                null  // sortBy null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: 거리순으로 정렬되어야 함
            for (int i = 0; i < result.size() - 1; i++) {
                assertThat(result.get(i).distance())
                    .isLessThanOrEqualTo(result.get(i + 1).distance());
            }
        }
    }

    // ==================== 페이지네이션 테스트 ====================

    @Nested
    @DisplayName("페이지네이션 테스트")
    class PaginationTests {

        @Test
        @DisplayName("첫 페이지 조회 - limit=2")
        void testFirstPage() {
            // given: 혼밥레벨 필터 없음
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, null, null, null),
                new CursorPaginationRequest(2, null),
                StoreFilteringRequest.SortBy.DISTANCE
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 3); // limit+1

            // then
            assertThat(result).hasSize(3); // hasNext=true를 위해 3개 조회됨
            assertThat(result.subList(0, 2))
                .extracting(row -> row.store().getName())
                .containsExactly("Store A", "Store C"); // 거리순: A < C < B < E
        }

        @Test
        @DisplayName("다음 페이지 조회 - 커서 기반")
        void testNextPage() {
            // given: 첫 페이지 조회 (혼밥레벨 필터 없음)
            StoreFilteringRequest firstRequest = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, null, null, null),
                new CursorPaginationRequest(2,null),
                StoreFilteringRequest.SortBy.DISTANCE
            );
            List<StoreRepositoryCustom.StoreRow> firstPage = storeRepositoryImpl.findStoresSlice(firstRequest, 3);

            // 커서 생성 (마지막 항목 기준)
            var lastRow = firstPage.get(1); // limit=2이므로 index 1이 마지막
            String cursor = KeysetCursor.encode(lastRow.distance(), lastRow.store().getId());

            // when: 두 번째 페이지 조회
            StoreFilteringRequest nextRequest = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, null, null, null),
                new CursorPaginationRequest(2, cursor),
                StoreFilteringRequest.SortBy.DISTANCE
            );
            List<StoreRepositoryCustom.StoreRow> nextPage = storeRepositoryImpl.findStoresSlice(nextRequest, 3);

            // then: 거리순 A < C < B < E 중 B, E
            assertThat(nextPage).hasSize(2);
            assertThat(nextPage)
                .extracting(row -> row.store().getName())
                .containsExactly("Store B", "Store E");

            // 첫 페이지와 중복 없음
            assertThat(nextPage)
                .extracting(row -> row.store().getId())
                .doesNotContainAnyElementsOf(
                    firstPage.subList(0, 2).stream()
                        .map(row -> row.store().getId())
                        .toList()
                );
        }

        @Test
        @DisplayName("마지막 페이지 - hasNext 판단")
        void testLastPage() {
            // given: 전체 4개 중 limit=5로 조회 (혼밥레벨 필터 없음)
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, null, null, null),
                new CursorPaginationRequest(5,null),
                StoreFilteringRequest.SortBy.DISTANCE
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 6); // limit+1

            // then
            assertThat(result).hasSize(4); // 전체 4개만 있음 (hasNext=false)
        }
    }

    // ==================== 경계값 테스트 ====================

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTests {

        @Test
        @DisplayName("정확히 5000m 거리의 매장 - 포함되어야 함")
        void testExactly5000mDistance() {
            // given: 강남역에서 정확히 5000m 거리에 매장 생성
            Store exactStore = createStore(
                "Exact 5km Store",
                CENTER_LAT + 0.045, CENTER_LON,
                Level.LEVEL_1,
                categoryKorean,
                50.0
            );

            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, null, null, null),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: 5000m 이내이므로 포함되어야 함
            assertThat(result)
                .extracting(row -> row.store().getName())
                .contains("Exact 5km Store");
        }

        @Test
        @DisplayName("가격 경계값 - min=max인 경우")
        void testPriceBoundary_MinEqualsMax() {
            // given: 정확히 12000원인 메뉴 검색
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(
                    new StoreFilteringRequest.PriceRange(12000, 12000),
                    null,
                    null,
                    null
                ),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactly("Store B"); // 초밥세트 12000원
        }

        @Test
        @DisplayName("혼밥레벨 경계값 - level=1 (최소값)")
        void testHonbobLevelBoundary_Min() {
            // given: honbobLevels == [1]
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, List.of(1), null, null),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Level 1인 Store A, Store C만
            assertThat(result)
                .extracting(row -> row.store().getName())
                .containsExactlyInAnyOrder("Store A", "Store C");
        }

        @Test
        @DisplayName("빈 결과 - 조건에 맞는 매장 없음")
        void testEmptyResult() {
            // given: 존재하지 않는 카테고리
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, null, null, List.of("중식")),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 결과 - 존재하지 않는 혼밥레벨")
        void testEmptyResult_InvalidHonbobLevel() {
            // given: Level 3 매장 요청 (Store D는 5km 밖)
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, List.of(3), null, null),
                null,
                null
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Store D만 Level 3이지만 5km 밖이므로 빈 결과
            assertThat(result).isEmpty();
        }
    }

    // ==================== 거리 계산 정확도 테스트 ====================

    @Nested
    @DisplayName("거리 계산 테스트")
    class DistanceCalculationTests {

        @Test
        @DisplayName("거리 계산 정확도 검증 - Level 2 매장")
        void testDistanceCalculation() {
            // given: Level 2 매장만 조회 (Store B만 해당)
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, List.of(2), null, null),
                null,
                StoreFilteringRequest.SortBy.DISTANCE
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Store B가 ~2579m
            assertThat(result).hasSize(1);
            assertThat(result.get(0).store().getName()).isEqualTo("Store B");
            assertThat(result.get(0).distance()).isBetween(2500, 2650); // ~2579m
        }

        @Test
        @DisplayName("거리 계산 정확도 검증 - Level 1 매장 (거리 순)")
        void testDistanceCalculation_Level1() {
            // given: Level 1 매장만 조회 (Store A, Store C)
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, List.of(1), null, null),
                null,
                StoreFilteringRequest.SortBy.DISTANCE
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: Store A(~417m)가 Store C(~2543m)보다 가까움
            assertThat(result).hasSize(2);
            assertThat(result.get(0).store().getName()).isEqualTo("Store A");
            assertThat(result.get(0).distance()).isBetween(400, 500); // ~417m
            assertThat(result.get(1).store().getName()).isEqualTo("Store C");
            assertThat(result.get(1).distance()).isBetween(2500, 2600); // ~2543m
        }

        @Test
        @DisplayName("동일 거리 매장 - ID 오름차순 정렬")
        void testSameDistance_SortById() {
            // given: 정확히 같은 위치에 두 매장 생성 (Level 1)
            Store store1 = createStore("Same Location 1", CENTER_LAT, CENTER_LON, Level.LEVEL_1, categoryKorean, 50.0);
            Store store2 = createStore("Same Location 2", CENTER_LAT, CENTER_LON, Level.LEVEL_1, categoryKorean, 50.0);

            // Level 1 매장만 조회
            StoreFilteringRequest request = createRequest(
                null,
                centerAt(CENTER_LAT, CENTER_LON),
                new StoreFilteringRequest.Filters(null, List.of(1), null, null),
                null,
                StoreFilteringRequest.SortBy.DISTANCE
            );

            // when
            List<StoreRepositoryCustom.StoreRow> result = storeRepositoryImpl.findStoresSlice(request, 20);

            // then: 거리가 같으면 ID 오름차순
            var sameDistanceStores = result.stream()
                .filter(row -> row.distance() == 0)
                .toList();

            for (int i = 0; i < sameDistanceStores.size() - 1; i++) {
                assertThat(sameDistanceStores.get(i).store().getId())
                    .isLessThan(sameDistanceStores.get(i + 1).store().getId());
            }
        }
    }

    // ==================== Helper Methods ====================

    private Store createStore(String name, double lat, double lon, Level honbobLevel,
                              PrimaryCategory category, Double internalScore) {
        Point location = point(lat, lon);
        Address address = Address.builder()
            .address(name + " Address")
            .latitude(lat)
            .longitude(lon)
            .location(location)
            .build();

        Categories categories = new Categories(category, null);

        return storeRepository.save(Store.builder()
            .name(name)
            .address(address)
            .honbobLevel(honbobLevel)
            .categories(categories)
            .internalScore(internalScore)
            .description(name + " Description")
            .build());
    }

    private void createMenu(Store store, String name, int price, boolean recommend) {
        menuRepository.save(Menu.builder()
            .store(store)
            .name(name)
            .price(price)
            .recommend(recommend)
            .build());
    }

    private void createSeatOption(Store store, SeatType seatType) {
        seatOptionRepository.save(SeatOption.builder()
            .store(store)
            .seatType(seatType)
            .build());
    }

    private static Point point(double lat, double lon) {
        Point p = GF.createPoint(new Coordinate(lon, lat));
        p.setSRID(4326);
        return p;
    }

    private static StoreFilteringRequest.Coordinate centerAt(double lat, double lon) {
        return new StoreFilteringRequest.Coordinate(lat, lon);
    }

    private StoreFilteringRequest createRequest(
            StoreFilteringRequest.BoundingBox bbox,
            StoreFilteringRequest.Coordinate center,
            StoreFilteringRequest.Filters filters,
            CursorPaginationRequest paging,
            StoreFilteringRequest.SortBy sortBy) {
        return new StoreFilteringRequest(bbox, center, filters, paging, sortBy);
    }
}