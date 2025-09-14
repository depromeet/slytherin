package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreDetailResponse;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.SeatOption;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import com.bobeat.backend.domain.store.repository.*;
import com.bobeat.backend.domain.store.vo.Categories;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.response.CursorPageResponse;
import lombok.RequiredArgsConstructor;
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

        Long nextCursor = hasNext && !data.isEmpty() ? data.getLast().id() : null;

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
        List<String> tags = new ArrayList<>(2);
        if (c.getPrimaryCategory() != null)   tags.add(c.getPrimaryCategory().getPrimaryType());
        if (c.getSecondaryCategory() != null) tags.add(c.getSecondaryCategory().getSecondaryType());
        return tags;
    }
}
