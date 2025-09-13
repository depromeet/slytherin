package com.bobeat.backend.domain.store.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.SeatOption;
import com.bobeat.backend.domain.store.entity.SeatType;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.global.db.PostgreSQLTestContainer;
import com.bobeat.backend.global.response.CursorPageResponse;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
@PostgreSQLTestContainer
public class StoreRepositoryImplTest {
    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreRepositoryImpl storeRepositoryImpl;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private SeatOptionRepository seatOptionRepository;

    private Store storeA;
    private Store storeB;

    @BeforeEach
    void setUp() {
        storeA = storeRepository.save(Store.builder()
                .name("Store A")
                .address(Address.builder()
                        .address("Seoul, Teheran-ro 123")
                        .latitude(37.5013)
                        .longitude(127.0396)
                        .build())
                .honbobLevel(3)
                .description("테스트 스토어 A")
                .build());

        storeB = storeRepository.save(Store.builder()
                .name("Store B")
                .address(Address.builder()
                        .address("Busan, Haeundae")
                        .latitude(35.1587)
                        .longitude(129.1604)
                        .build())
                .honbobLevel(5)
                .description("테스트 스토어 B")
                .build());

        // 메뉴 (A: 추천 8,000 / 일반 12,000,  B: 추천 15,000)
        menuRepository.save(Menu.builder()
                .store(storeA).name("추천 메뉴 A1").price(8_000).recommend(true).build());
        menuRepository.save(Menu.builder()
                .store(storeA).name("일반 메뉴 A2").price(12_000).recommend(false).build());
        menuRepository.save(Menu.builder()
                .store(storeB).name("추천 메뉴 B1").price(15_000).recommend(true).build());

        // 좌석 옵션 (A: FOR_ONE, B: BAR)
        seatOptionRepository.save(SeatOption.builder()
                .store(storeA).seatType(SeatType.FOR_ONE).maxCapacity(1).build());
        seatOptionRepository.save(SeatOption.builder()
                .store(storeB).seatType(SeatType.BAR_TABLE).maxCapacity(4).build());
    }

    @Test
    void 필터_추천메뉴_가격범위() {
        // given: 추천 7,000~10,000 → A(8,000)만
        StoreFilteringRequest.Filters filters = new StoreFilteringRequest.Filters(
                new StoreFilteringRequest.PriceRange(7_000, 10_000),
                5,
                null,
                null,
                null
        );
        StoreFilteringRequest req = new StoreFilteringRequest(null, null, filters, null);

        // when
        CursorPageResponse<Store> res = storeRepositoryImpl.search(req);

        // then
        assertThat(res.getData()).extracting(Store::getName)
                .containsExactly("Store A");
    }

    @Test
    void 필터_좌석타입_IN() {
        // given: FOR_ONE 좌석 매장만
        StoreFilteringRequest.Filters filters = new StoreFilteringRequest.Filters(
                null,
                5,
                List.of(SeatType.FOR_ONE),
                null,
                null
        );
        StoreFilteringRequest req = new StoreFilteringRequest(null, null, filters, null);

        // when
        CursorPageResponse<Store> res = storeRepositoryImpl.search(req);

        // then
        assertThat(res.getData()).extracting(Store::getName)
                .containsExactly("Store A");
    }

    @Test
    void 필터_혼밥레벨_이하() {
        // given: level ≤ 3 → A(3)만
        StoreFilteringRequest.Filters filters = new StoreFilteringRequest.Filters(
                null,
                3,
                null,
                null,
                null
        );
        StoreFilteringRequest req = new StoreFilteringRequest(null, null, filters, null);

        // when
        CursorPageResponse<Store> res = storeRepositoryImpl.search(req);

        // then
        assertThat(res.getData()).extracting(Store::getHonbobLevel)
                .containsExactly(3);
        assertThat(res.getData()).extracting(Store::getName)
                .containsExactly("Store A");
    }

    @Test
    void 필터_BBox_내부만() {
        // given: BBox를 서울 인근으로 지정 → A만 포함
        StoreFilteringRequest.BoundingBox bbox = new StoreFilteringRequest.BoundingBox(
                new StoreFilteringRequest.Coordinate(37.7, 127.0), // NW(lat, lon)
                new StoreFilteringRequest.Coordinate(37.3, 127.3)  // SE(lat, lon)
        );
        StoreFilteringRequest.Filters filters = new StoreFilteringRequest.Filters(
                null, 5, null, null, null
        );
        StoreFilteringRequest req = new StoreFilteringRequest(
                bbox,
                new StoreFilteringRequest.Coordinate(37.55, 127.05),
                filters,
                null
        );

        // when
        CursorPageResponse<Store> res = storeRepositoryImpl.search(req);

        // then
        assertThat(res.getData()).extracting(Store::getName)
                .containsExactly("Store A");
    }
}
