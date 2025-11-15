package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.dto.request.StoreCreateRequest;
import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.PrimaryCategory;
import com.bobeat.backend.domain.store.entity.SeatOption;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import com.bobeat.backend.domain.store.repository.MenuRepository;
import com.bobeat.backend.domain.store.repository.PrimaryCategoryRepository;
import com.bobeat.backend.domain.store.repository.SeatOptionRepository;
import com.bobeat.backend.domain.store.repository.StoreImageRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.domain.store.vo.Categories;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StoreCreateService {

    private final PrimaryCategoryRepository primaryCategoryRepository;
    private final GeometryFactory geometryFactory;
    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;
    private final MenuRepository menuRepository;
    private final SeatOptionRepository seatOptionRepository;
    private final StoreEmbeddingService storeEmbeddingService;

    @Transactional
    public List<Long> createStores(List<StoreCreateRequest> requests) {
        return requests.stream()
                .map(this::createStore)
                .toList();
    }

    @Transactional
    public Long createStore(StoreCreateRequest request) {
        validateDuplicateStore(request);

        PrimaryCategory primaryCategory = primaryCategoryRepository.findByPrimaryType(
                        request.categories().primaryCategory())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STORE_CATEGORY));

        Address address = createAddress(request.address());
        Categories categories = createCategories(primaryCategory);

        Store store = Store.builder()
                .name(request.name())
                .address(address)
                .phoneNumber(request.phoneNumber())
                .description(request.description())
                .honbobLevel(Level.fromValue(request.honbobLevel()))
                .categories(categories)
                .build();

        Store savedStore = storeRepository.save(store);

        createStoreImages(request.storeImages(), savedStore);
        createMenus(request.menus(), savedStore);
        createSeatOptions(request.seatOptions(), savedStore);

        storeEmbeddingService.saveEmbeddingByStore(savedStore.getId());

        return savedStore.getId();
    }


    private void validateDuplicateStore(StoreCreateRequest request) {
        if (storeRepository.existsByNameAndAddress_LatitudeAndAddress_Longitude(
                request.name(),
                request.address().latitude(),
                request.address().longitude())) {
            throw new CustomException(ErrorCode.DUPLICATE_STORE_NAME);
        }
    }

    private Address createAddress(StoreCreateRequest.AddressRequest addressRequest) {
        Point location = geometryFactory.createPoint(
                new Coordinate(addressRequest.longitude(), addressRequest.latitude())
        );
        location.setSRID(4326);

        Address address = Address.builder()
                .address(addressRequest.address())
                .latitude(addressRequest.latitude())
                .longitude(addressRequest.longitude())
                .build();

        address.setLocation(location);
        return address;
    }

    private Categories createCategories(PrimaryCategory primaryCategory) {
        return new Categories(primaryCategory, null);
    }

    private void createStoreImages(List<StoreCreateRequest.StoreImageRequest> imageRequests, Store store) {
        List<StoreImage> storeImages = imageRequests.stream()
                .map(imageRequest -> StoreImage.builder()
                        .store(store)
                        .imageUrl(imageRequest.imageUrl())
                        .isMain(imageRequest.isMain())
                        .build())
                .toList();

        storeImageRepository.saveAll(storeImages);
    }

    private void createMenus(List<StoreCreateRequest.MenuRequest> menuRequests, Store store) {
        List<Menu> menus = menuRequests.stream()
                .map(menuRequest -> Menu.builder()
                        .store(store)
                        .name(menuRequest.name())
                        .price(menuRequest.price())
                        .imageUrl(menuRequest.imageUrl())
                        .build())
                .toList();

        menuRepository.saveAll(menus);
    }

    private void createSeatOptions(List<StoreCreateRequest.SeatOptionRequest> seatOptionRequests, Store store) {
        List<SeatOption> seatOptions = seatOptionRequests.stream()
                .map(seatOptionRequest -> SeatOption.builder()
                        .store(store)
                        .seatType(seatOptionRequest.seatType())
                        .imageUrl(seatOptionRequest.imageUrl())
                        .build())
                .toList();

        seatOptionRepository.saveAll(seatOptions);
    }
}
