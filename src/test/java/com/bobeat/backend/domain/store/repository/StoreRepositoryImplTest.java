package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.SeatOption;
import com.bobeat.backend.domain.store.entity.SeatType;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.global.response.CursorPageResponse;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
//@PostgreSQLTestContainer
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

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    private static Point point(double lat, double lon) {
        // JTS: x=lon, y=lat 주의!
        Point p = GF.createPoint(new Coordinate(lon, lat));
        p.setSRID(4326);
        return p;
    }

    private static StoreFilteringRequest.Coordinate centerAt(double lat, double lon) {
        // center는 항상 존재하도록
        return new StoreFilteringRequest.Coordinate(lat, lon);
    }

    @BeforeEach
    void setUp() {
        // Store A: 서울
        storeA = storeRepository.save(Store.builder()
                .name("Store A")
                .address(Address.builder()
                        .address("Seoul, Teheran-ro 123")
                        .latitude(37.5013)
                        .longitude(127.0396)
                        .location(point(37.5013, 127.0396))
                        .build())
                .honbobLevel(3)
                .description("테스트 스토어 A")
                .build());

        // Store B: 부산
        storeB = storeRepository.save(Store.builder()
                .name("Store B")
                .address(Address.builder()
                        .address("Busan, Haeundae")
                        .latitude(35.1587)
                        .longitude(129.1604)
                        .location(point(35.1587, 129.1604))
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

        // 좌석 옵션 (A: FOR_ONE, B: BAR_TABLE)
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

        // center를 서울(스토어 A 근처)로 지정 → 반경 기본 700m
        StoreFilteringRequest req = new StoreFilteringRequest(
                null,                                      // bbox 없음 → center 반경만 적용
                centerAt(37.5013, 127.0396),               // Store A 좌표와 동일
                filters,
                null
        );

        // when
        List<StoreSearchResultDto> res = storeRepositoryImpl.search(req);

        // then
        assertThat(res).extracting(StoreSearchResultDto::name)
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

        StoreFilteringRequest req = new StoreFilteringRequest(
                null,
                centerAt(37.5013, 127.0396),  // 서울 중심 → A만 반경 내
                filters,
                null
        );

        // when
        List<StoreSearchResultDto> res = storeRepositoryImpl.search(req);

        // then
        assertThat(res).extracting(StoreSearchResultDto::name)
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

        StoreFilteringRequest req = new StoreFilteringRequest(
                null,
                centerAt(37.5013, 127.0396),  // 서울 중심
                filters,
                null
        );

        // when
        List<StoreSearchResultDto> res = storeRepositoryImpl.search(req);

        // then
        assertThat(res).extracting(StoreSearchResultDto::honbobLevel)
                .containsExactly(3, 1);
        assertThat(res).extracting(StoreSearchResultDto::name)
                .containsExactly("Store A");
    }

    @Test
    void 필터_BBox_내부만() {
        // given: BBox를 서울 인근으로 지정 → A만 포함 (BBox 유효하면 BBox만 적용)
        StoreFilteringRequest.BoundingBox bbox = new StoreFilteringRequest.BoundingBox(
                new StoreFilteringRequest.Coordinate(37.7, 127.0), // NW(lat, lon)
                new StoreFilteringRequest.Coordinate(37.3, 127.3)  // SE(lat, lon)
        );

        StoreFilteringRequest.Filters filters = new StoreFilteringRequest.Filters(
                null, 5, null, null, null
        );

        StoreFilteringRequest req = new StoreFilteringRequest(
                bbox,
                centerAt(37.55, 127.05), // 있어도 BBox가 유효하면 BBox만 적용
                filters,
                null
        );

        // when
        List<StoreSearchResultDto> res = storeRepositoryImpl.search(req);

        // then
        assertThat(res).extracting(StoreSearchResultDto::name)
                .containsExactly("Store A");
    }
}
