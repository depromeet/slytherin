package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.store.entity.EmbeddingStatus;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.external.clova.service.ClovaEmbeddingClient;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreEmbeddingBatchService {

    private static final int BATCH_SIZE = 100; // 한 번에 처리할 개수

    private final StoreRepository storeRepository;
    private final StoreEmbeddingService storeEmbeddingService;
    private final ClovaEmbeddingClient clovaEmbeddingClient;

    /**
     * 10분마다 실행되는 임베딩 배치 작업
     * PostgreSQL의 Store 데이터를 읽어 pgvector 임베딩을 생성하고 동기화합니다.
     *
     * Status 방식:
     * - PENDING 또는 FAILED 상태인 Store만 조회
     * - 성공 시 COMPLETED, 실패 시 FAILED로 상태 변경
     */
    @Scheduled(fixedDelay = 600000) // 10분 (600,000ms)
    @Transactional
    public void processEmbeddingBatch() {
        // 1. [Reader] PENDING 또는 FAILED 상태의 Store 조회 (최대 100개)
        Page<Store> storePage = storeRepository.findByEmbeddingStatusIn(
                List.of(EmbeddingStatus.PENDING, EmbeddingStatus.FAILED),
                PageRequest.of(0, BATCH_SIZE)
        );

        List<Store> storesToProcess = storePage.getContent();

        if (storesToProcess.isEmpty()) {
            log.debug("처리할 임베딩 작업이 없습니다.");
            return;
        }

        log.info("임베딩 배치 시작: {}건 처리 예정", storesToProcess.size());

        // 2. [Processor] Store 목록을 임베딩할 텍스트 목록으로 변환
        List<String> textsToEmbed = storesToProcess.stream()
                .map(storeEmbeddingService::buildStoreText)
                .toList();

        // 3. [External Call] CLOVA API 병렬 호출
        List<List<Double>> vectors;
        try {
            vectors = clovaEmbeddingClient.getEmbeddingsParallelSync(textsToEmbed);
        } catch (Exception e) {
            log.error("CLOVA API 병렬 요청 실패: {}", e.getMessage(), e);
            // 실패한 Store들을 FAILED 상태로 변경
            storesToProcess.forEach(Store::failEmbedding);
            return;
        }

        // 4. [Writer] 결과 매핑 및 저장
        // 4-1. 병렬 처리 중 일부가 실패하여 개수가 안 맞으면 처리
        if (storesToProcess.size() != vectors.size()) {
            log.warn("임베딩 요청 개수({})와 성공 개수({}) 불일치. 개별 처리로 전환.",
                    storesToProcess.size(), vectors.size());

            // 개별적으로 재시도
            int successCount = 0;
            int failCount = 0;

            for (Store store : storesToProcess) {
                try {
                    String text = storeEmbeddingService.buildStoreText(store);
                    List<Double> vector = clovaEmbeddingClient.getEmbeddingSync(text);

                    float[] vectorFloat = convertToFloatArray(vector);
                    store.completeEmbedding(new PGvector(vectorFloat));
                    successCount++;
                } catch (Exception e) {
                    log.error("Store ID {} 임베딩 생성 실패: {}", store.getId(), e.getMessage());
                    store.failEmbedding();
                    failCount++;
                }
            }

            log.info("임베딩 배치 완료 (개별 처리): 성공 {}건, 실패 {}건", successCount, failCount);
            return;
        }

        // 4-2. Store 엔티티에 벡터 값 설정 및 상태 업데이트
        int successCount = 0;
        for (int i = 0; i < storesToProcess.size(); i++) {
            Store store = storesToProcess.get(i);
            List<Double> vector = vectors.get(i);

            try {
                float[] vectorFloat = convertToFloatArray(vector);
                store.completeEmbedding(new PGvector(vectorFloat));
                successCount++;
            } catch (Exception e) {
                log.error("Store ID {} 벡터 변환 실패: {}", store.getId(), e.getMessage());
                store.failEmbedding();
            }
        }

        // 4-3. @Transactional에 의해 커밋 시점에 자동으로 UPDATE 쿼리 실행됨 (더티 체킹)

        log.info("임베딩 배치 완료: 성공 {}건, 실패 {}건",
                successCount, storesToProcess.size() - successCount);
    }

    /**
     * List<Double>을 float[]로 변환합니다. (PGvector는 float[] 사용)
     */
    private float[] convertToFloatArray(List<Double> vector) {
        float[] result = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            result[i] = vector.get(i).floatValue();
        }
        return result;
    }
}
