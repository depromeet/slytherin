package com.bobeat.backend.domain.store.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.entity.EmbeddingStatus;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreEmbedding;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.global.db.PostgreSQLTestContainer;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
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
 * SimilarStoreRepository 통합 테스트
 * PostGIS 거리 필터링 기능을 검증합니다.
 */
@SpringBootTest
@Transactional
@PostgreSQLTestContainer
class SimilarStoreRepositoryImplTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SimilarStoreRepository similarStoreRepository;

    @Autowired
    private StoreEmbeddingRepository storeEmbeddingRepository;

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    private Store targetStore;
    private Store nearbyStore1;
    private Store nearbyStore2;
    private Store farStore;

    /**
     * JTS Point 생성 헬퍼 메서드
     * @param lat 위도
     * @param lon 경도
     * @return Point 객체 (SRID 4326)
     */
    private static Point createPoint(double lat, double lon) {
        // JTS: x=경도(lon), y=위도(lat) 주의!
        Point point = GF.createPoint(new Coordinate(lon, lat));
        point.setSRID(4326);
        return point;
    }

    @BeforeEach
    void setUp() {
        // 기준 가게: 강남역 (37.4979, 127.0276)
        targetStore = storeRepository.save(Store.builder()
                .name("강남 파스타 맛집")
                .address(Address.builder()
                        .address("서울시 강남구 강남대로 123")
                        .latitude(37.4979)
                        .longitude(127.0276)
                        .location(createPoint(37.4979, 127.0276))
                        .build())
                .honbobLevel(Level.fromValue(3))
                .description("기준 가게")
                .phoneNumber("02-1234-5678")
                .build());

        // 근처 가게 1: 약 1.5km 떨어진 가게
        // 강남역에서 북쪽으로 약 1.5km (위도 +0.0135)
        nearbyStore1 = storeRepository.save(Store.builder()
                .name("역삼 이탈리안")
                .address(Address.builder()
                        .address("서울시 강남구 테헤란로 456")
                        .latitude(37.5114)
                        .longitude(127.0276)
                        .location(createPoint(37.5114, 127.0276))
                        .build())
                .honbobLevel(Level.fromValue(2))
                .description("1.5km 거리의 가게")
                .phoneNumber("02-2345-6789")
                .build());

        // 근처 가게 2: 약 2.5km 떨어진 가게
        // 강남역에서 북쪽으로 약 2.5km (위도 +0.0225)
        nearbyStore2 = storeRepository.save(Store.builder()
                .name("선릉 레스토랑")
                .address(Address.builder()
                        .address("서울시 강남구 논현로 789")
                        .latitude(37.5204)
                        .longitude(127.0276)
                        .location(createPoint(37.5204, 127.0276))
                        .build())
                .honbobLevel(Level.fromValue(4))
                .description("2.5km 거리의 가게")
                .phoneNumber("02-3456-7890")
                .build());

        // 먼 가게: 약 5km 떨어진 가게
        // 강남역에서 북쪽으로 약 5km (위도 +0.045)
        farStore = storeRepository.save(Store.builder()
                .name("잠실 음식점")
                .address(Address.builder()
                        .address("서울시 송파구 올림픽로 123")
                        .latitude(37.5429)
                        .longitude(127.0276)
                        .location(createPoint(37.5429, 127.0276))
                        .build())
                .honbobLevel(Level.fromValue(1))
                .description("5km 거리의 가게")
                .phoneNumber("02-4567-8901")
                .build());
    }

    @Test
    @DisplayName("3km 이내 가게만 필터링 - 2개 반환")
    void findNearbyStores_Within3km_Success() {
        // given
        double maxDistance = 3000.0; // 3km

        // when
        List<Long> nearbyStoreIds = similarStoreRepository.findNearbyStoreIds(
                targetStore.getId(),
                maxDistance
        );

        // then
        assertThat(nearbyStoreIds).hasSize(2);
        assertThat(nearbyStoreIds).containsExactlyInAnyOrder(
                nearbyStore1.getId(),
                nearbyStore2.getId()
        );
        assertThat(nearbyStoreIds).doesNotContain(farStore.getId());
    }

    @Test
    @DisplayName("1km 이내 가게만 필터링 - 0개 반환")
    void findNearbyStores_Within1km_Empty() {
        // given
        double maxDistance = 1000.0; // 1km

        // when
        List<Long> nearbyStoreIds = similarStoreRepository.findNearbyStoreIds(
                targetStore.getId(),
                maxDistance
        );

        // then
        assertThat(nearbyStoreIds).isEmpty();
    }

    @Test
    @DisplayName("2km 이내 가게만 필터링 - 1개 반환")
    void findNearbyStores_Within2km_OneStore() {
        // given
        double maxDistance = 2000.0; // 2km

        // when
        List<Long> nearbyStoreIds = similarStoreRepository.findNearbyStoreIds(
                targetStore.getId(),
                maxDistance
        );

        // then
        assertThat(nearbyStoreIds).hasSize(1);
        assertThat(nearbyStoreIds).containsExactly(nearbyStore1.getId());
    }

    @Test
    @DisplayName("10km 이내 가게 필터링 - 3개 반환 (먼 가게 포함)")
    void findNearbyStores_Within10km_AllStores() {
        // given
        double maxDistance = 10000.0; // 10km

        // when
        List<Long> nearbyStoreIds = similarStoreRepository.findNearbyStoreIds(
                targetStore.getId(),
                maxDistance
        );

        // then
        assertThat(nearbyStoreIds).hasSize(3);
        assertThat(nearbyStoreIds).containsExactlyInAnyOrder(
                nearbyStore1.getId(),
                nearbyStore2.getId(),
                farStore.getId()
        );
    }

    @Test
    @DisplayName("location이 null인 가게는 제외")
    void findNearbyStores_ExcludeNullLocation() {
        // given
        Store storeWithoutLocation = storeRepository.save(Store.builder()
                .name("위치 없는 가게")
                .address(Address.builder()
                        .address("서울시 강남구 어딘가")
                        .latitude(null)
                        .longitude(null)
                        .location(null) // location이 null
                        .build())
                .honbobLevel(Level.fromValue(3))
                .description("위치 정보 없음")
                .phoneNumber("02-0000-0000")
                .build());

        double maxDistance = 10000.0; // 10km

        // when
        List<Long> nearbyStoreIds = similarStoreRepository.findNearbyStoreIds(
                targetStore.getId(),
                maxDistance
        );

        // then
        assertThat(nearbyStoreIds).doesNotContain(storeWithoutLocation.getId());
    }

    @Test
    @DisplayName("자기 자신은 결과에서 제외")
    void findNearbyStores_ExcludeSelf() {
        // given
        double maxDistance = 100.0; // 100m (매우 가까운 거리)

        // when
        List<Long> nearbyStoreIds = similarStoreRepository.findNearbyStoreIds(
                targetStore.getId(),
                maxDistance
        );

        // then
        assertThat(nearbyStoreIds).doesNotContain(targetStore.getId());
    }

    @Test
    @DisplayName("존재하지 않는 가게 ID로 조회 - 빈 리스트 반환")
    void findNearbyStores_NonExistentStoreId_Empty() {
        // given
        Long nonExistentStoreId = 99999L;
        double maxDistance = 3000.0; // 3km

        // when
        List<Long> nearbyStoreIds = similarStoreRepository.findNearbyStoreIds(
                nonExistentStoreId,
                maxDistance
        );

        // then
        assertThat(nearbyStoreIds).isEmpty();
    }

    /**
     * 임베딩 벡터 유사도 정렬 테스트
     */
    @Nested
    @DisplayName("findSimilarByEmbedding 테스트")
    class FindSimilarByEmbeddingTest {

        private Store embeddingTargetStore;
        private Store similarStore1;
        private Store similarStore2;
        private Store differentStore;

        /**
         * 1024차원 임베딩 벡터 생성 헬퍼 메서드
         * @param seed 시드값 (벡터 특성 결정)
         * @return 1024차원 임베딩 벡터
         */
        private List<Double> createEmbeddingVector(double seed) {
            List<Double> vector = new ArrayList<>(1024);
            for (int i = 0; i < 1024; i++) {
                // seed 값에 따라 벡터 생성 (간단한 패턴)
                vector.add(Math.sin(seed + i * 0.01));
            }
            return vector;
        }

        @BeforeEach
        void setUpEmbedding() {
            // 임베딩 테스트용 가게들 생성
            embeddingTargetStore = storeRepository.save(Store.builder()
                    .name("타겟 가게")
                    .address(Address.builder()
                            .address("서울시 강남구")
                            .latitude(37.5000)
                            .longitude(127.0300)
                            .location(createPoint(37.5000, 127.0300))
                            .build())
                    .honbobLevel(Level.fromValue(3))
                    .description("임베딩 기준 가게")
                    .phoneNumber("02-1111-1111")
                    .build());

            similarStore1 = storeRepository.save(Store.builder()
                    .name("유사 가게 1")
                    .address(Address.builder()
                            .address("서울시 강남구")
                            .latitude(37.5010)
                            .longitude(127.0300)
                            .location(createPoint(37.5010, 127.0300))
                            .build())
                    .honbobLevel(Level.fromValue(3))
                    .description("타겟과 유사한 가게 1")
                    .phoneNumber("02-2222-2222")
                    .build());

            similarStore2 = storeRepository.save(Store.builder()
                    .name("유사 가게 2")
                    .address(Address.builder()
                            .address("서울시 강남구")
                            .latitude(37.5020)
                            .longitude(127.0300)
                            .location(createPoint(37.5020, 127.0300))
                            .build())
                    .honbobLevel(Level.fromValue(3))
                    .description("타겟과 유사한 가게 2")
                    .phoneNumber("02-3333-3333")
                    .build());

            differentStore = storeRepository.save(Store.builder()
                    .name("전혀 다른 가게")
                    .address(Address.builder()
                            .address("서울시 강남구")
                            .latitude(37.5030)
                            .longitude(127.0300)
                            .location(createPoint(37.5030, 127.0300))
                            .build())
                    .honbobLevel(Level.fromValue(3))
                    .description("타겟과 다른 가게")
                    .phoneNumber("02-4444-4444")
                    .build());

            // 임베딩 벡터 생성 및 저장
            // 타겟: seed=1.0
            // similarStore1: seed=1.1 (타겟과 유사)
            // similarStore2: seed=1.2 (타겟과 약간 유사)
            // differentStore: seed=5.0 (타겟과 다름)

            storeEmbeddingRepository.save(StoreEmbedding.builder()
                    .store(embeddingTargetStore)
                    .embedding(createEmbeddingVector(1.0))
                    .embeddingStatus(EmbeddingStatus.COMPLETED)
                    .build());

            storeEmbeddingRepository.save(StoreEmbedding.builder()
                    .store(similarStore1)
                    .embedding(createEmbeddingVector(1.1))
                    .embeddingStatus(EmbeddingStatus.COMPLETED)
                    .build());

            storeEmbeddingRepository.save(StoreEmbedding.builder()
                    .store(similarStore2)
                    .embedding(createEmbeddingVector(1.2))
                    .embeddingStatus(EmbeddingStatus.COMPLETED)
                    .build());

            storeEmbeddingRepository.save(StoreEmbedding.builder()
                    .store(differentStore)
                    .embedding(createEmbeddingVector(5.0))
                    .embeddingStatus(EmbeddingStatus.COMPLETED)
                    .build());
        }

        @Test
        @DisplayName("임베딩 유사도 순으로 정렬 - 유사한 순서대로 반환")
        void findSimilarByEmbedding_OrderBySimilarity() {
            // given
            List<Long> candidateIds = List.of(
                    similarStore1.getId(),
                    similarStore2.getId(),
                    differentStore.getId()
            );

            // when
            List<Store> results = similarStoreRepository.findSimilarByEmbedding(
                    embeddingTargetStore.getId(),
                    candidateIds,
                    3
            );

            // then
            assertThat(results).hasSize(3);
            // 유사도 순서: similarStore1 (1.1) > similarStore2 (1.2) > differentStore (5.0)
            assertThat(results.get(0).getId()).isEqualTo(similarStore1.getId());
            assertThat(results.get(1).getId()).isEqualTo(similarStore2.getId());
            assertThat(results.get(2).getId()).isEqualTo(differentStore.getId());
        }

        @Test
        @DisplayName("limit 적용 - 상위 N개만 반환")
        void findSimilarByEmbedding_ApplyLimit() {
            // given
            List<Long> candidateIds = List.of(
                    similarStore1.getId(),
                    similarStore2.getId(),
                    differentStore.getId()
            );
            int limit = 2;

            // when
            List<Store> results = similarStoreRepository.findSimilarByEmbedding(
                    embeddingTargetStore.getId(),
                    candidateIds,
                    limit
            );

            // then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getId()).isEqualTo(similarStore1.getId());
            assertThat(results.get(1).getId()).isEqualTo(similarStore2.getId());
        }

        @Test
        @DisplayName("빈 후보군 - 빈 리스트 반환")
        void findSimilarByEmbedding_EmptyCandidates() {
            // given
            List<Long> emptyCandidates = List.of();

            // when
            List<Store> results = similarStoreRepository.findSimilarByEmbedding(
                    embeddingTargetStore.getId(),
                    emptyCandidates,
                    5
            );

            // then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("임베딩 상태가 COMPLETED가 아닌 가게 제외")
        void findSimilarByEmbedding_ExcludeNonCompletedEmbedding() {
            // given: PENDING 상태의 가게 추가
            Store pendingStore = storeRepository.save(Store.builder()
                    .name("PENDING 가게")
                    .address(Address.builder()
                            .address("서울시 강남구")
                            .latitude(37.5040)
                            .longitude(127.0300)
                            .location(createPoint(37.5040, 127.0300))
                            .build())
                    .honbobLevel(Level.fromValue(3))
                    .description("임베딩 대기 중")
                    .phoneNumber("02-5555-5555")
                    .build());

            storeEmbeddingRepository.save(StoreEmbedding.builder()
                    .store(pendingStore)
                    .embedding(createEmbeddingVector(1.05))
                    .embeddingStatus(EmbeddingStatus.PENDING) // PENDING 상태
                    .build());

            List<Long> candidateIds = List.of(
                    similarStore1.getId(),
                    pendingStore.getId()
            );

            // when
            List<Store> results = similarStoreRepository.findSimilarByEmbedding(
                    embeddingTargetStore.getId(),
                    candidateIds,
                    5
            );

            // then: PENDING 상태는 제외되고 COMPLETED만 반환
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(similarStore1.getId());
            assertThat(results).doesNotContain(pendingStore);
        }

        @Test
        @DisplayName("임베딩 벡터가 null인 가게 제외")
        void findSimilarByEmbedding_ExcludeNullEmbedding() {
            // given: 임베딩이 null인 가게
            Store noEmbeddingStore = storeRepository.save(Store.builder()
                    .name("임베딩 없는 가게")
                    .address(Address.builder()
                            .address("서울시 강남구")
                            .latitude(37.5050)
                            .longitude(127.0300)
                            .location(createPoint(37.5050, 127.0300))
                            .build())
                    .honbobLevel(Level.fromValue(3))
                    .description("임베딩 없음")
                    .phoneNumber("02-6666-6666")
                    .build());

            storeEmbeddingRepository.save(StoreEmbedding.builder()
                    .store(noEmbeddingStore)
                    .embedding(null) // embedding이 null
                    .embeddingStatus(EmbeddingStatus.COMPLETED)
                    .build());

            List<Long> candidateIds = List.of(
                    similarStore1.getId(),
                    noEmbeddingStore.getId()
            );

            // when
            List<Store> results = similarStoreRepository.findSimilarByEmbedding(
                    embeddingTargetStore.getId(),
                    candidateIds,
                    5
            );

            // then: null 임베딩은 제외
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(similarStore1.getId());
            assertThat(results).doesNotContain(noEmbeddingStore);
        }

        @Test
        @DisplayName("타겟 가게의 임베딩이 없으면 빈 리스트 반환")
        void findSimilarByEmbedding_TargetHasNoEmbedding() {
            // given: 임베딩이 없는 타겟 가게
            Store targetWithoutEmbedding = storeRepository.save(Store.builder()
                    .name("임베딩 없는 타겟")
                    .address(Address.builder()
                            .address("서울시 강남구")
                            .latitude(37.5060)
                            .longitude(127.0300)
                            .location(createPoint(37.5060, 127.0300))
                            .build())
                    .honbobLevel(Level.fromValue(3))
                    .description("타겟 임베딩 없음")
                    .phoneNumber("02-7777-7777")
                    .build());

            List<Long> candidateIds = List.of(
                    similarStore1.getId(),
                    similarStore2.getId()
            );

            // when
            List<Store> results = similarStoreRepository.findSimilarByEmbedding(
                    targetWithoutEmbedding.getId(),
                    candidateIds,
                    5
            );

            // then
            assertThat(results).isEmpty();
        }
    }
}
