package com.bobeat.backend.domain.store.service;

import static com.bobeat.backend.global.exception.ErrorCode.NOT_FOUND_STORE;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreDetailResponse;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.SeatOption;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import com.bobeat.backend.domain.store.repository.MenuRepository;
import com.bobeat.backend.domain.store.repository.SeatOptionRepository;
import com.bobeat.backend.domain.store.repository.StoreImageRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.domain.store.repository.StoreRepositoryCustom;
import com.bobeat.backend.domain.store.vo.Categories;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.response.CursorPageResponse;
import com.bobeat.backend.global.util.KeysetCursor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;
    private final MenuRepository menuRepository;
    private final SeatOptionRepository seatOptionRepository;

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

        // N+1 해결: 배치 조회
        var repMenuMap = storeRepository.findRepresentativeMenus(storeIds);
        var seatTypesMap = storeRepository.findSeatTypes(storeIds);
        var mainImageMap = storeImageRepository.findMainImagesByStoreIds(storeIds)
                .stream()
                .collect(Collectors.toMap(
                        img -> img.getStore().getId(),
                        img -> img
                ));

        List<StoreSearchResultDto> data = rows.stream()
                .map(r -> {
                    var store = r.store();
                    var mainImage = mainImageMap.get(store.getId());

                    long id = store.getId();
                    int distance = r.distance();
                    int walkingMinutes = (int) Math.ceil(distance / 80.0);

                    return new StoreSearchResultDto(
                            store.getId(),
                            store.getName(),
                            mainImage != null ? mainImage.getImageUrl() : null, // Null 안전성
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

    private List<Menu> sortMenuByRecommend(List<Menu> menus1, List<Menu> menus2) {
        menus1.addAll(menus2);
        return menus1.stream()
                .sorted(Comparator.comparing(Menu::isRecommend)).toList().reversed();
    }


    public List<String> buildTagsFromCategories(Categories c) {
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
}
