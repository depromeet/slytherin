package com.bobeat.backend.domain.store.event;

import com.bobeat.backend.domain.store.service.StoreEmbeddingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Store 관련 이벤트를 처리하는 리스너
 *
 * 트랜잭션 커밋 후 실행되어야 하는 후처리 작업을 담당합니다.
 * - 임베딩 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreEventListener {

    private final StoreEmbeddingService storeEmbeddingService;

    /**
     * 가게 생성 트랜잭션이 성공적으로 커밋된 후 실행됩니다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStoreCreation(StoreCreationEvent event) {
        List<Long> storeIds = event.getStoreIds();
        storeIds.forEach(storeEmbeddingService::saveEmbeddingByStoreAsync);
    }
}
