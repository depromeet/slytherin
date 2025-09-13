package com.bobeat.backend.domain.store.service;

import static com.bobeat.backend.global.exception.ErrorCode.NOT_FOUND_RESTAURANT;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreDetailResponse;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResponse;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.SeatOption;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import com.bobeat.backend.domain.store.repository.MenuRepository;
import com.bobeat.backend.domain.store.repository.SeatOptionRepository;
import com.bobeat.backend.domain.store.repository.StoreImageRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.response.CursorPageResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;
    private final MenuRepository menuRepository;
    private final SeatOptionRepository seatOptionRepository;

    @Transactional(readOnly = true)
    public CursorPageResponse<StoreSearchResultDto> search(StoreFilteringRequest request) {
        int pageSize = request.paging() != null ? request.paging().limit() : 20;
        
        List<StoreSearchResultDto> dataPlusOne = storeRepository.search(request);

        return CursorPageResponse.of(
                dataPlusOne,
                pageSize,
                StoreSearchResultDto::id
        );
    }

    public StoreDetailResponse findById(Long restaurantId) {
        Store store = storeRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_RESTAURANT));
        List<StoreImage> storeImages = storeImageRepository.findByStore(store);
        List<Menu> menus = menuRepository.findByStore(store);
        List<SeatOption> seatOptions = seatOptionRepository.findByStore(store);
        return StoreDetailResponse.of(store, storeImages, menus, seatOptions);
    }

    private StoreSearchResponse convertToDto(Store store) {
        return new StoreSearchResponse(null, null); // 임시 반환
    }
}
