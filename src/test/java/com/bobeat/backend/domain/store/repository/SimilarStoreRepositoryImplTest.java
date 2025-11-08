package com.bobeat.backend.domain.store.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.dto.StoreWithDistance;
import com.bobeat.backend.domain.store.entity.EmbeddingStatus;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreEmbedding;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.global.db.PostgreSQLTestContainer;
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
     *
     * @param lat 위도
     * @param lon 경도
     *
     * @return Point 객체 (SRID 4326)
     */
    private static Point createPoint(double lat, double lon) {
        // JTS: x=경도(lon), y=위도(lat) 주의!
        Point point = GF.createPoint(new Coordinate(lon, lat));
        point.setSRID(4326);
        return point;
    }

    /**
     * 1024차원 임베딩 벡터 생성 헬퍼 메서드
     *
     * @param seed 시드값 (벡터 특성 결정)
     * @return 1024차원 임베딩 벡터
     */
    private static float[] createEmbeddingVector(float seed) {
        float[] vector = new float[1024];
        for (int i = 0; i < 1024; i++) {
            vector[i] = (float) (Math.sin(seed + i * 0.01f));
        }
        return vector;
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
    @DisplayName("3km 이내 가게만 필터링 - honbobLevel 필터링 적용하여 1개 반환")
    void findNearbyStores_Within3km_Success() {
        // given
        double maxDistance = 3000.0; // 3km
        // targetStore: honbobLevel = 3
        // nearbyStore1: honbobLevel = 2 (1.5km) - 포함 (레벨 3 이하)
        // nearbyStore2: honbobLevel = 4 (2.5km) - 제외 (레벨 3 초과)

        // when
        List<Long> nearbyStoreIds = similarStoreRepository.findNearbyStoreIds(
                targetStore.getId(),
                maxDistance
        );

        // then
        assertThat(nearbyStoreIds).hasSize(1);
        assertThat(nearbyStoreIds).containsExactly(nearbyStore1.getId());
        assertThat(nearbyStoreIds).doesNotContain(nearbyStore2.getId());
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
    @DisplayName("10km 이내 가게 필터링 - honbobLevel 필터링 적용하여 2개 반환")
    void findNearbyStores_Within10km_AllStores() {
        // given
        double maxDistance = 10000.0; // 10km
        // targetStore: honbobLevel = 3
        // nearbyStore1: honbobLevel = 2 (1.5km) - 포함 (레벨 3 이하)
        // nearbyStore2: honbobLevel = 4 (2.5km) - 제외 (레벨 3 초과)
        // farStore: honbobLevel = 1 (5km) - 포함 (레벨 3 이하)

        // when
        List<Long> nearbyStoreIds = similarStoreRepository.findNearbyStoreIds(
                targetStore.getId(),
                maxDistance
        );

        // then
        assertThat(nearbyStoreIds).hasSize(2);
        assertThat(nearbyStoreIds).containsExactlyInAnyOrder(
                nearbyStore1.getId(),
                farStore.getId()
        );
        assertThat(nearbyStoreIds).doesNotContain(nearbyStore2.getId());
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

    @Test
    @DisplayName("혼밥 레벨 필터링 - 기준 가게의 honbobLevel 이하인 가게만 반환")
    void findNearbyStores_FilterByHonbobLevel() {
        // given
        double maxDistance = 10000.0; // 충분히 큰 거리 (10km)

        // targetStore: honbobLevel = 3
        // nearbyStore1: honbobLevel = 2 - 포함되어야 함 (레벨 3 이하)
        // nearbyStore2: honbobLevel = 4 - 제외되어야 함 (레벨 3 초과)
        // farStore: honbobLevel = 1 - 포함되어야 함 (레벨 3 이하)

        // when
        List<Long> nearbyStoreIds = similarStoreRepository.findNearbyStoreIds(
                targetStore.getId(),
                maxDistance
        );

        // then
        assertThat(nearbyStoreIds).hasSize(2);
        assertThat(nearbyStoreIds).containsExactlyInAnyOrder(
                nearbyStore1.getId(), // honbobLevel = 2
                farStore.getId()       // honbobLevel = 1
        );
        assertThat(nearbyStoreIds).doesNotContain(nearbyStore2.getId()); // honbobLevel = 4 (제외)
    }

    @Test
    @DisplayName("혼밥 레벨 필터링 - 동일한 레벨도 포함")
    void findNearbyStores_IncludeSameHonbobLevel() {
        // given
        // 기준 가게와 동일한 혼밥 레벨(3)을 가진 가게 추가
        Store sameLevelStore = storeRepository.save(Store.builder()
                .name("동일 레벨 가게")
                .address(Address.builder()
                        .address("서울시 강남구 테헤란로 100")
                        .latitude(37.5100)
                        .longitude(127.0276)
                        .location(createPoint(37.5100, 127.0276))
                        .build())
                .honbobLevel(Level.fromValue(3)) // targetStore와 동일한 레벨
                .description("동일 혼밥 레벨")
                .phoneNumber("02-1111-2222")
                .build());

        double maxDistance = 10000.0; // 10km

        // when
        List<Long> nearbyStoreIds = similarStoreRepository.findNearbyStoreIds(
                targetStore.getId(),
                maxDistance
        );

        // then - 동일한 레벨(3)도 포함되어야 함
        assertThat(nearbyStoreIds).contains(sameLevelStore.getId());
    }

    /**
     * 임베딩 벡터 유사도 정렬 + 유저 위치 기준 거리 계산 테스트
     */
    @Nested
    @DisplayName("findSimilarByEmbeddingWithDistance 테스트")
    class FindSimilarByEmbeddingWithDistanceTest {

        private Store embeddingTargetStore;
        private Store similarStore1;
        private Store similarStore2;
        private Store differentStore;

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
            storeEmbeddingRepository.save(StoreEmbedding.builder()
                    .store(embeddingTargetStore)
                    .embedding(createEmbeddingVector(1.0f))
                    .embeddingStatus(EmbeddingStatus.COMPLETED)
                    .build());

            storeEmbeddingRepository.save(StoreEmbedding.builder()
                    .store(similarStore1)
                    .embedding(createEmbeddingVector(1.1f))
                    .embeddingStatus(EmbeddingStatus.COMPLETED)
                    .build());

            storeEmbeddingRepository.save(StoreEmbedding.builder()
                    .store(similarStore2)
                    .embedding(createEmbeddingVector(1.2f))
                    .embeddingStatus(EmbeddingStatus.COMPLETED)
                    .build());

            storeEmbeddingRepository.save(StoreEmbedding.builder()
                    .store(differentStore)
                    .embedding(createEmbeddingVector(5.0f))
                    .embeddingStatus(EmbeddingStatus.COMPLETED)
                    .build());
        }

        @Test
        @DisplayName("임베딩 유사도 순 정렬 + 유저 위치 기준 거리 계산")
        void findSimilarByEmbeddingWithDistance_Success() {
            // given
            List<Long> candidateIds = List.of(
                    similarStore1.getId(),
                    similarStore2.getId(),
                    differentStore.getId()
            );
            // 유저 위치: 강남역 (37.4979, 127.0276)
            Double userLatitude = 37.4979;
            Double userLongitude = 127.0276;

            // when
            List<StoreWithDistance> results = similarStoreRepository.findSimilarByEmbeddingWithDistance(
                    embeddingTargetStore.getId(),
                    candidateIds,
                    3,
                    userLatitude,
                    userLongitude
            );

            // then
            assertThat(results).hasSize(3);
            // 유사도 순서: similarStore1 > similarStore2 > differentStore
            assertThat(results.get(0).getStore().getId()).isEqualTo(similarStore1.getId());
            assertThat(results.get(1).getStore().getId()).isEqualTo(similarStore2.getId());
            assertThat(results.get(2).getStore().getId()).isEqualTo(differentStore.getId());

            // 거리가 Integer로 반환되는지 확인
            assertThat(results.get(0).getDistance()).isInstanceOf(Integer.class);
            assertThat(results.get(0).getDistance()).isGreaterThan(0);
        }

        @Test
        @DisplayName("거리 계산이 정수로 반환되는지 확인")
        void findSimilarByEmbeddingWithDistance_ReturnsIntegerDistance() {
            // given
            List<Long> candidateIds = List.of(similarStore1.getId());
            Double userLatitude = 37.5000;
            Double userLongitude = 127.0300;

            // when
            List<StoreWithDistance> results = similarStoreRepository.findSimilarByEmbeddingWithDistance(
                    embeddingTargetStore.getId(),
                    candidateIds,
                    1,
                    userLatitude,
                    userLongitude
            );

            // then
            assertThat(results).hasSize(1);
            Integer distance = results.get(0).getDistance();
            assertThat(distance).isNotNull();
            assertThat(distance).isInstanceOf(Integer.class);
        }

        @Test
        @DisplayName("limit 적용 - 상위 N개만 반환")
        void findSimilarByEmbeddingWithDistance_ApplyLimit() {
            // given
            List<Long> candidateIds = List.of(
                    similarStore1.getId(),
                    similarStore2.getId(),
                    differentStore.getId()
            );
            int limit = 2;
            Double userLatitude = 37.4979;
            Double userLongitude = 127.0276;

            // when
            List<StoreWithDistance> results = similarStoreRepository.findSimilarByEmbeddingWithDistance(
                    embeddingTargetStore.getId(),
                    candidateIds,
                    limit,
                    userLatitude,
                    userLongitude
            );

            // then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getStore().getId()).isEqualTo(similarStore1.getId());
            assertThat(results.get(1).getStore().getId()).isEqualTo(similarStore2.getId());
        }

        @Test
        @DisplayName("빈 후보군 - 빈 리스트 반환")
        void findSimilarByEmbeddingWithDistance_EmptyCandidates() {
            // given
            List<Long> emptyCandidates = List.of();
            Double userLatitude = 37.4979;
            Double userLongitude = 127.0276;

            // when
            List<StoreWithDistance> results = similarStoreRepository.findSimilarByEmbeddingWithDistance(
                    embeddingTargetStore.getId(),
                    emptyCandidates,
                    5,
                    userLatitude,
                    userLongitude
            );

            // then
            assertThat(results).isEmpty();
        }
    }
}
