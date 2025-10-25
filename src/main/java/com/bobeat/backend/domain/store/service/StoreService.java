package com.bobeat.backend.domain.store.service;

import static com.bobeat.backend.global.exception.ErrorCode.NOT_FOUND_STORE;

import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.member.service.MemberService;
import com.bobeat.backend.domain.store.dto.request.StoreCreateRequest;
import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.SearchHistoryDto;
import com.bobeat.backend.domain.store.dto.response.StoreDetailResponse;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
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
import com.bobeat.backend.domain.store.repository.StoreRepositoryCustom;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.domain.store.vo.Categories;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import com.bobeat.backend.global.response.CursorPageResponse;
import com.bobeat.backend.global.util.KeysetCursor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;
    private final MenuRepository menuRepository;
    private final SeatOptionRepository seatOptionRepository;
    private final PrimaryCategoryRepository primaryCategoryRepository;
    private final GeometryFactory geometryFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final MemberService memberService;
    private final StoreEmbeddingService storeEmbeddingService;

    @Transactional(readOnly = true)
    public CursorPageResponse<StoreSearchResultDto> search(StoreFilteringRequest request) {
        final int pageSize = request.paging() != null ? request.paging().limit() : 20;

        // DB 쿼리 레벨에서 복합 점수 기반으로 정렬됨 (30% 내부점수 + 70% 거리)
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
                    var store = r.store();
                    var mainImage = storeImageRepository.findByStoreAndIsMainTrue(store);

                    long id = store.getId();
                    int distance = r.distance();
                    int walkingMinutes = (int) Math.ceil(distance / 80.0);

                    return new StoreSearchResultDto(
                            store.getId(),
                            store.getName(),
                            mainImage.getImageUrl(),
                            repMenuMap.getOrDefault(id, new StoreSearchResultDto.SignatureMenu(null, 0)),
                            new StoreSearchResultDto.Coordinate(store.getAddress().getLatitude(),
                                    store.getAddress().getLongitude()),
                            distance,
                            walkingMinutes,
                            seatTypesMap.getOrDefault(id, List.of()),
                            buildTagsFromCategories(store.getCategories()),
                            store.getHonbobLevel() != null ? store.getHonbobLevel().getValue() : 0
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
        //List<Menu> menus = menuRepository.findByStore(store);

        List<Menu> menus1 = menuRepository.findTop3ByStoreAndRecommendFalseOrderByIdAsc(store);
        List<Menu> menus2 = menuRepository.findTop3ByStoreAndRecommendTrueOrderByIdAsc(store);
        List<Menu> sortMenus = sortMenuByRecommend(menus1, menus2);
        List<SeatOption> seatOptions = seatOptionRepository.findByStore(store);
        return StoreDetailResponse.of(store, storeImages, sortMenus, seatOptions);
    }

    private List<String> buildTagsFromCategories(Categories c) {
        if (c == null) {
            return List.of();
        }
        List<String> tags = new ArrayList<>(5);
        if (c.getPrimaryCategory() != null) {
            tags.add(c.getPrimaryCategory().getPrimaryType());
        }
        if (c.getSecondaryCategory() != null) {
            tags.add(c.getSecondaryCategory().getSecondaryType());
        }
        return tags;
    }

    @Transactional
    public List<Long> createStores(List<StoreCreateRequest> requests) {

        List<Long> storeIds = requests.stream()
                .map(this::createStore)
                .toList();

        storeIds.forEach(storeid -> storeEmbeddingService.saveEmbeddingByStore(storeid));
        return storeIds;
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
                .honbobLevel(Level.fromValue(request.honbobLevel()))
                .categories(categories)
                .build();

        Store savedStore = storeRepository.save(store);

        createStoreImages(request.storeImages(), savedStore);
        createMenus(request.menus(), savedStore);
        createSeatOptions(request.seatOptions(), savedStore);

        return savedStore.getId();
    }

    public List<StoreSearchResultDto> searchStore(Long userId, String query) {
        List<Store> stores = storeRepository.findAll().stream()
                .limit(5)
                .toList();
        List<Long> storeIds = stores.stream()
                .map(Store::getId)
                .toList();
        Map<Long, List<String>> seatTypes = storeRepository.findSeatTypes(storeIds);

        List<StoreSearchResultDto> storeSearchResultDtos = stores.stream()
                .map(store -> {
                    StoreImage storeimage = storeImageRepository.findByStoreAndIsMainTrue(store);
                    List<String> seatTypeNames = seatTypes.getOrDefault(store.getId(), List.of());
                    List<String> tagNames = buildTagsFromCategories(store.getCategories());
                    return StoreSearchResultDto.of(store, storeimage, seatTypeNames, tagNames);
                })
                .toList();
        saveSearchHistory(userId, query);

        return storeSearchResultDtos;
    }

    public List<SearchHistoryDto> findSearchHistory() {
        return List.of();
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

    private List<Menu> sortMenuByRecommend(List<Menu> menus1, List<Menu> menus2) {
        menus1.addAll(menus2);
        return menus1.stream()
                .sorted(Comparator.comparing(Menu::isRecommend)).toList().reversed();
    }

    private void saveSearchHistory(Long userId, String name) {
    }
}
