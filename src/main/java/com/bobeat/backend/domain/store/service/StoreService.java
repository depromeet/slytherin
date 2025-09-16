package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.dto.request.StoreCreateRequest;
import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreDetailResponse;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.entity.*;
import com.bobeat.backend.domain.store.repository.*;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.domain.store.vo.Categories;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import com.bobeat.backend.global.response.CursorPageResponse;
import com.bobeat.backend.global.util.KeysetCursor;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.bobeat.backend.global.exception.ErrorCode.NOT_FOUND_STORE;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;
    private final MenuRepository menuRepository;
    private final SeatOptionRepository seatOptionRepository;
    private final PrimaryCategoryRepository primaryCategoryRepository;
    private final GeometryFactory geometryFactory;

    @Transactional(readOnly = true)
    public CursorPageResponse<StoreSearchResultDto> search(StoreFilteringRequest request) {
        final int pageSize = request.paging() != null ? request.paging().limit() : 20;

        List<StoreRepositoryCustom.StoreRow> rows = storeRepository.findStoresSlice(request, pageSize + 1);

        boolean hasNext = rows.size() > pageSize;
        if (hasNext) {
            rows = rows.subList(0, pageSize);
        }

        List<Long> storeIds = rows.stream()
                .map(r -> r.store().getId())
                .toList();

        var repMenuMap = storeRepository.findRepresentativeMenus(storeIds);
        var seatTypesMap = storeRepository.findSeatTypes(storeIds);

        List<StoreSearchResultDto> data = rows.stream()
                .map(r -> {
                    var s = r.store();
                    long id = s.getId();
                    int distance = r.distance();
                    int walkingMinutes = (int) Math.ceil(distance / 80.0);

                    return new StoreSearchResultDto(
                            s.getId(),
                            s.getName(),
                            s.getMainImageUrl(),
                            repMenuMap.getOrDefault(id, new StoreSearchResultDto.SignatureMenu(null, 0)),
                            new StoreSearchResultDto.Coordinate(s.getAddress().getLatitude(), s.getAddress().getLongitude()),
                            distance,
                            walkingMinutes,
                            seatTypesMap.getOrDefault(id, List.of()),
                            buildTagsFromCategories(s.getCategories()),
                            s.getHonbobLevel() != null ? s.getHonbobLevel().getValue() : 0
                    );
                })
                .collect(Collectors.toList());

        String nextCursor = null;
        if (hasNext && !rows.isEmpty()) {
            var last = rows.getLast();
            nextCursor = KeysetCursor.encode(last.distance(), last.store().getId());
        }

        return new CursorPageResponse<>(data, nextCursor, hasNext, null);
    }

    public StoreDetailResponse findById(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_STORE));
        List<StoreImage> storeImages = storeImageRepository.findByStore(store);
        List<Menu> menus = menuRepository.findByStore(store);
        List<SeatOption> seatOptions = seatOptionRepository.findByStore(store);
        return StoreDetailResponse.of(store, storeImages, menus, seatOptions);
    }

    private List<String> buildTagsFromCategories(Categories c) {
        if (c == null) return List.of();
        List<String> tags = new ArrayList<>(5);
        if (c.getPrimaryCategory() != null) tags.add(c.getPrimaryCategory().getPrimaryType());
        if (c.getSecondaryCategory() != null) tags.add(c.getSecondaryCategory().getSecondaryType());
        return tags;
    }

    @Transactional
    public List<Long> createStores(List<StoreCreateRequest> requests) {
        return requests.stream()
                .map(this::createStore)
                .toList();
    }

    @Transactional
    public Long createStore(StoreCreateRequest request) {
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
                .mainImageUrl(request.mainImageUrl())
                .honbobLevel(Level.fromValue(request.honbobLevel()))
                .categories(categories)
                .build();

        Store savedStore = storeRepository.save(store);

        createStoreImages(request.storeImages(), savedStore);
        createMenus(request.menus(), savedStore);
        createSeatOptions(request.seatOptions(), savedStore);

        return savedStore.getId();
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
