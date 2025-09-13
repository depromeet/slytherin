package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;

import java.util.List;

public interface StoreRepositoryCustom {
    List<StoreSearchResultDto> search(StoreFilteringRequest request);
}
