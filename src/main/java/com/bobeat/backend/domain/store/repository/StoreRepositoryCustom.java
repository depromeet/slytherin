package com.bobeat.backend.domain.store.repository;

import com.bobeat.backend.domain.store.dto.request.StoreFilteringRequest;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.entity.Store;

import java.util.List;
import java.util.Map;

public interface StoreRepositoryCustom {
//    List<StoreSearchResultDto> search(StoreFilteringRequest request);

    List<StoreRow> findStoresSlice(StoreFilteringRequest request, int limitPlusOne);

    Map<Long, StoreSearchResultDto.SignatureMenu> findRepresentativeMenus(List<Long> storeIds);

    Map<Long, List<String>> findSeatTypes(List<Long> storeIds);

    record StoreRow(Store store, int distance) {}
}
