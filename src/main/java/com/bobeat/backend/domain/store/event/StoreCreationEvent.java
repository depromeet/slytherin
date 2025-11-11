package com.bobeat.backend.domain.store.event;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class StoreCreationEvent {
    /**
     * 생성된 가게 ID 리스트
     */
    private final List<Long> storeIds;
}