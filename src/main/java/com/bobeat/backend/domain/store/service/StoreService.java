package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResponse;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.global.response.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public CursorPageResponse<StoreSearchResponse> search(StoreFilteringRequest request) {
        CursorPageResponse<Store> storeCursorPageResponse = storeRepository.search(request);

        List<StoreSearchResponse> storeSearchResponses = storeCursorPageResponse.getData().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new CursorPageResponse<>(
                storeSearchResponses,
                storeCursorPageResponse.getNextCursor(),
                storeCursorPageResponse.getHasNext(),
                storeCursorPageResponse.getMetadata()
        );
    }

    private StoreSearchResponse convertToDto(Store store) {
        return new StoreSearchResponse(null, null); // 임시 반환
    }
}
