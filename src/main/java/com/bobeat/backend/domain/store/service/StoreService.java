package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.global.response.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

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
}
