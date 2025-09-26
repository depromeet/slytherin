package com.bobeat.backend.domain.store.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bobeat.backend.domain.store.dto.request.StoreCreateRequest;
import com.bobeat.backend.domain.store.dto.request.StoreUpdateRequest;
import com.bobeat.backend.domain.store.entity.PrimaryCategory;
import com.bobeat.backend.domain.store.entity.SeatType;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.repository.MenuRepository;
import com.bobeat.backend.domain.store.repository.PrimaryCategoryRepository;
import com.bobeat.backend.domain.store.repository.SeatOptionRepository;
import com.bobeat.backend.domain.store.repository.StoreImageRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.domain.store.service.StoreService;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.domain.store.vo.Categories;
import com.bobeat.backend.global.db.PostgreSQLTestContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@PostgreSQLTestContainer
@Transactional
class AdminStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreImageRepository storeImageRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private SeatOptionRepository seatOptionRepository;

    @Autowired
    private PrimaryCategoryRepository primaryCategoryRepository;

    private PrimaryCategory primaryCategory;

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    private static Point point(double lat, double lon) {
        Point p = GF.createPoint(new Coordinate(lon, lat));
        p.setSRID(4326);
        return p;
    }

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        storeImageRepository.deleteAll();
        menuRepository.deleteAll();
        seatOptionRepository.deleteAll();
        storeRepository.deleteAll();
        primaryCategoryRepository.deleteAll();

        // 테스트용 카테고리 생성
        primaryCategory = PrimaryCategory.builder()
                .primaryType("한식")
                .build();
        primaryCategoryRepository.save(primaryCategory);
    }

    @Test
    @DisplayName("어드민이 새로운 가게를 등록한다")
    void createStores() throws Exception {
        List<StoreCreateRequest> requests = List.of(
                createStoreCreateRequest("맛있는 한식당", "서울시 강남구", 127.0276, 37.4979)
        );

        mockMvc.perform(post("/api/admin/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.response").isArray())
                .andExpect(jsonPath("$.response.length()").value(1))
                .andExpect(jsonPath("$.response[0]").isNumber());

        // 실제 DB에 저장되었는지 확인
        List<Store> stores = storeRepository.findAll();
        assertEquals(1, stores.size());
        assertEquals("맛있는 한식당", stores.get(0).getName());
    }

    @Test
    @DisplayName("어드민이 기존 가게 정보를 수정한다")
    void updateStore() throws Exception {
        // 테스트용 가게 생성
        Store savedStore = createAndSaveTestStore("기존 가게", "서울시 종로구");

        StoreUpdateRequest updateRequest = createStoreUpdateRequest(
                "수정된 가게명", "서울시 마포구", 126.9568, 37.5665
        );

        mockMvc.perform(put("/api/admin/stores/{storeId}", savedStore.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isNumber())
                .andExpect(jsonPath("$.response").value(savedStore.getId()));

        // 실제 DB에서 수정되었는지 확인
        Store updatedStore = storeRepository.findById(savedStore.getId()).get();
        assertEquals("수정된 가게명", updatedStore.getName());
        assertEquals("서울시 마포구", updatedStore.getAddress().getAddress());
    }

    @Test
    @DisplayName("가게 수정시 이미지, 메뉴, 좌석옵션이 모두 교체된다")
    void updateStoreWithAllData() throws Exception {
        // 테스트용 가게 생성
        Store savedStore = createAndSaveTestStore("기존 가게", "서울시 종로구");

        StoreUpdateRequest updateRequest = new StoreUpdateRequest(
                "수정된 가게명",
                new StoreUpdateRequest.AddressUpdateRequest("서울시 마포구", 37.5665, 126.9568),
                "02-1234-5678",
                "수정된 설명",
                "https://example.com/updated-main.jpg",
                3,
                new StoreUpdateRequest.CategoryUpdateRequest("한식"),
                List.of(
                        new StoreUpdateRequest.StoreImageUpdateRequest("https://example.com/updated1.jpg", true),
                        new StoreUpdateRequest.StoreImageUpdateRequest("https://example.com/updated2.jpg", false)
                ),
                List.of(
                        new StoreUpdateRequest.MenuUpdateRequest("수정된 메뉴1", 15000, "https://example.com/menu1.jpg"),
                        new StoreUpdateRequest.MenuUpdateRequest("수정된 메뉴2", 18000, "https://example.com/menu2.jpg")
                ),
                List.of(
                        new StoreUpdateRequest.SeatOptionUpdateRequest(SeatType.FOR_ONE,
                                "https://example.com/seat1.jpg"),
                        new StoreUpdateRequest.SeatOptionUpdateRequest(SeatType.FOR_TWO,
                                "https://example.com/seat2.jpg")
                )
        );

        mockMvc.perform(put("/api/admin/stores/{storeId}", savedStore.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(savedStore.getId()));

        // 실제 DB에서 변경사항 확인
        Store updatedStore = storeRepository.findById(savedStore.getId()).get();
        assertEquals("수정된 가게명", updatedStore.getName());
        assertEquals("서울시 마포구", updatedStore.getAddress().getAddress());
    }

    private Store createAndSaveTestStore(String name, String address) {
        Store store = Store.builder()
                .name(name)
                .address(createTestAddress(address))
                .phoneNumber("02-1234-5678")
                .description("테스트 설명")
                .mainImageUrl("https://example.com/test.jpg")
                .honbobLevel(com.bobeat.backend.domain.member.entity.Level.LEVEL_2)
                .categories(createTestCategories())
                .build();

        return storeRepository.save(store);
    }

    private Address createTestAddress(String address) {
        Point location = point(37.5665, 126.9784);
        com.bobeat.backend.domain.store.vo.Address addr = com.bobeat.backend.domain.store.vo.Address.builder()
                .address(address)
                .latitude(37.5665)
                .longitude(126.9784)
                .build();
        addr.setLocation(location);
        return addr;
    }

    private Categories createTestCategories() {
        return new Categories(primaryCategory, null);
    }


    private StoreCreateRequest createStoreCreateRequest(String name, String address, double longitude,
                                                        double latitude) {
        return new StoreCreateRequest(
                name,
                new StoreCreateRequest.AddressRequest(address, latitude, longitude),
                "02-1234-5678",
                "맛있는 음식을 제공하는 식당입니다",
                "https://example.com/main.jpg",
                2,
                new StoreCreateRequest.CategoryRequest("한식"),
                List.of(
                        new StoreCreateRequest.StoreImageRequest("https://example.com/image1.jpg", true),
                        new StoreCreateRequest.StoreImageRequest("https://example.com/image2.jpg", false)
                ),
                List.of(
                        new StoreCreateRequest.MenuRequest("메뉴1", 12000, "https://example.com/menu1.jpg"),
                        new StoreCreateRequest.MenuRequest("메뉴2", 15000, "https://example.com/menu2.jpg")
                ),
                List.of(
                        new StoreCreateRequest.SeatOptionRequest(SeatType.FOR_ONE, "https://example.com/seat1.jpg"),
                        new StoreCreateRequest.SeatOptionRequest(SeatType.FOR_TWO, "https://example.com/seat2.jpg")
                )
        );
    }

    private StoreUpdateRequest createStoreUpdateRequest(String name, String address, double longitude,
                                                        double latitude) {
        return new StoreUpdateRequest(
                name,
                new StoreUpdateRequest.AddressUpdateRequest(address, latitude, longitude),
                "02-9876-5432",
                "수정된 설명입니다",
                "https://example.com/updated-main.jpg",
                3,
                new StoreUpdateRequest.CategoryUpdateRequest("한식"),
                List.of(
                        new StoreUpdateRequest.StoreImageUpdateRequest("https://example.com/updated1.jpg", true),
                        new StoreUpdateRequest.StoreImageUpdateRequest("https://example.com/updated2.jpg", false)
                ),
                List.of(
                        new StoreUpdateRequest.MenuUpdateRequest("수정된 메뉴1", 14000,
                                "https://example.com/updated-menu1.jpg"),
                        new StoreUpdateRequest.MenuUpdateRequest("수정된 메뉴2", 16000,
                                "https://example.com/updated-menu2.jpg")
                ),
                List.of(
                        new StoreUpdateRequest.SeatOptionUpdateRequest(SeatType.FOR_ONE,
                                "https://example.com/updated-seat1.jpg"),
                        new StoreUpdateRequest.SeatOptionUpdateRequest(SeatType.BAR_TABLE,
                                "https://example.com/updated-seat2.jpg")
                )
        );
    }

}
