package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.global.response.CursorPageResponse;

public interface StoreRepositoryCustom {
    CursorPageResponse<Store> search(StoreFilteringRequest request);
}
