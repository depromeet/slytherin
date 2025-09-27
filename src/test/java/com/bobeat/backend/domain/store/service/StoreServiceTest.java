package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.store.repository.MenuRepository;
import com.bobeat.backend.domain.store.repository.SeatOptionRepository;
import com.bobeat.backend.domain.store.repository.StoreImageRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreImageRepository storeImageRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private SeatOptionRepository seatOptionRepository;

    @InjectMocks
    private StoreService storeService;

//    @Test
//    void 가게_상세_조회_할_수_있다() {
//        // given
//        Long storeId = 1L;
//
//        Store store = Store.builder()
//                .id(storeId)
//                .name("테스트 식당")
//                .address(Address.builder()
//                        .address("광진구")
//                        .latitude(Double.valueOf(12.2))
//                        .longitude(Double.valueOf(12.2))
//                        .build())
//                .phoneNumber("010-1234-5678")
//                .honbobLevel(Level.fromValue(3))
//                .build();
//
//        List<StoreImage> storeImages = List.of(
//                StoreImage.builder()
//                        .id(1L)
//                        .imageUrl("main.jpg")
//                        .isMain(true)
//                        .store(store)
//                        .build(),
//                StoreImage.builder()
//                        .id(2L)
//                        .imageUrl("sub.jpg")
//                        .isMain(false)
//                        .store(store)
//                        .build()
//        );
//
//        List<Menu> menus = List.of(
//                Menu.builder()
//                        .id(1L)
//                        .name("김치찌개")
//                        .price(9000)
//                        .recommend(true)
//                        .store(store)
//                        .imageUrl("김치찌개.jpg")
//                        .build(),
//                Menu.builder()
//                        .id(2L)
//                        .name("된장찌개")
//                        .price(9000)
//                        .recommend(false)
//                        .store(store)
//                        .imageUrl("된장찌개.jpg")
//                        .build()
//        );
//
//        List<SeatOption> seatOptions = List.of(
//                SeatOption.builder().seatType(SeatType.FOR_ONE).build(),
//                SeatOption.builder().seatType(SeatType.BAR_TABLE).build()
//        );
//
//        // when
//        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
//        when(storeImageRepository.findByStore(store)).thenReturn(storeImages);
//        when(menuRepository.findByStore(store)).thenReturn(menus);
//        when(seatOptionRepository.findByStore(store)).thenReturn(seatOptions);
//
//        // then
//        StoreDetailResponse response = storeService.findById(storeId);
//
//        assertNotNull(response);
//        Assertions.assertThat(response.name()).isEqualTo("테스트 식당");
//        Assertions.assertThat(response.thumbnailUrls().size()).isEqualTo(2);
//        //Assertions.assertThat(response.menus().size()).isEqualTo(2);
//        Assertions.assertThat(response.seatImages().size()).isEqualTo(2);
////        Assertions.assertThat(response.menus())
////                .extracting("imageUrl")
////                .containsExactlyInAnyOrder("김치찌개.jpg", "된장찌개.jpg");
//    }
}
