package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.store.controller.StoreEmbeddingTestController.EmbeddingTestResponse;
import com.bobeat.backend.domain.store.controller.StoreEmbeddingTestController.StoreTextResponse;
import com.bobeat.backend.domain.store.entity.Menu;
import com.bobeat.backend.domain.store.entity.SeatOption;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreEmbedding;
import com.bobeat.backend.domain.store.external.clova.service.ClovaEmbeddingClient;
import com.bobeat.backend.domain.store.repository.MenuRepository;
import com.bobeat.backend.domain.store.repository.SeatOptionRepository;
import com.bobeat.backend.domain.store.repository.StoreEmbeddingRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StoreEmbeddingService {

    private final ClovaEmbeddingClient clovaEmbeddingClient;
    private final MenuRepository menuRepository;
    private final SeatOptionRepository seatOptionRepository;
    private final StoreRepository storeRepository;
    private final StoreEmbeddingRepository storeEmbeddingRepository;

    /**
     * Store 엔티티의 모든 정보를 결합하여 임베딩 벡터를 생성합니다.
     * <p>
     * 포함 정보: - 가게 이름 - 설명 - 혼밥 난이도 - 카테고리 (Primary, Secondary) - 위치 (주소) - 메뉴 (이름, 가격) - 좌석 유형
     *
     * @param store Store 엔티티
     * @return 1024차원의 임베딩 벡터
     */
    public Mono<List<Double>> generateStoreEmbedding(Store store) {
        String combinedText = buildStoreText(store);
        return clovaEmbeddingClient.getEmbedding(combinedText);
    }

    /**
     * Store 엔티티의 모든 정보를 결합하여 임베딩 벡터를 동기식으로 생성합니다.
     *
     * @param store Store 엔티티
     * @return 1024차원의 임베딩 벡터
     */
    public List<Double> generateStoreEmbeddingSync(Store store) {
        String combinedText = buildStoreText(store);
        return clovaEmbeddingClient.getEmbeddingSync(combinedText);
    }

    @Transactional
    public EmbeddingTestResponse saveEmbeddingByStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STORE));
        String combinedText = buildStoreText(store);
        List<Double> embedding = generateStoreEmbeddingSync(store);

        StoreEmbedding storeEmbedding = StoreEmbedding.builder()
                .embedding(embedding)
                .store(store)
                .build();

        saveOrUpdateStoreEmbedding(storeEmbedding, store);
        return new EmbeddingTestResponse(
                storeId,
                store.getName(),
                combinedText,
                embedding,
                embedding.size()
        );
    }

    public StoreTextResponse createEmbeddingTextByStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STORE));

        String combinedText = buildStoreText(store);

        return new StoreTextResponse(
                storeId,
                store.getName(),
                combinedText,
                combinedText.length()
        );
    }

    /**
     * Store 엔티티의 모든 정보를 하나의 텍스트로 결합합니다.
     *
     * @param store Store 엔티티
     * @return 결합된 텍스트
     */
    private String buildStoreText(Store store) {

        StringBuilder sb = new StringBuilder();

        // 1. 가게 이름
        if (store.getName() != null) {
            sb.append("가게명: ").append(store.getName()).append("\n");
        }

        // 2. 설명
        if (store.getDescription() != null) {
            sb.append("설명: ").append(store.getDescription()).append("\n");
        }

        // 3. 혼밥 난이도
        if (store.getHonbobLevel() != null) {
            sb.append("혼밥 난이도: ").append(store.getHonbobLevel().getDescription())
                    .append(" (레벨 ").append(store.getHonbobLevel().getValue()).append(")\n");
        }

        // 4. 카테고리
        if (store.getCategories() != null) {
            if (store.getCategories().getPrimaryCategory() != null) {
                sb.append("주요 카테고리: ").append(store.getCategories().getPrimaryCategory().getPrimaryType()).append("\n");
            }
            if (store.getCategories().getSecondaryCategory() != null) {
                sb.append("부 카테고리: ").append(store.getCategories().getSecondaryCategory().getSecondaryType())
                        .append("\n");
            }
        }

        // 5. 위치 (주소)
        if (store.getAddress() != null && store.getAddress().getAddress() != null) {
            sb.append("주소: ").append(store.getAddress().getAddress()).append("\n");
        }

        // 6. 메뉴 (이름, 가격)
        List<Menu> menus = menuRepository.findByStore(store);
        if (!menus.isEmpty()) {
            sb.append("메뉴: ");
            String menuText = menus.stream()
                    .map(menu -> menu.getName() + " (" + formatPrice(menu.getPrice()) + "원)")
                    .collect(Collectors.joining(", "));
            sb.append(menuText).append("\n");
        }

        // 7. 좌석 유형
        List<SeatOption> seatOptions = seatOptionRepository.findByStore(store);
        if (!seatOptions.isEmpty()) {
            sb.append("좌석 유형: ");
            String seatText = seatOptions.stream()
                    .map(seat -> seat.getSeatType().name())
                    .distinct()
                    .collect(Collectors.joining(", "));
            sb.append(seatText).append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * 가격을 천 단위 구분자로 포맷팅합니다.
     *
     * @param price 가격
     * @return 포맷팅된 가격 문자열
     */
    private String formatPrice(int price) {
        return String.format("%,d", price);
    }

    private void saveOrUpdateStoreEmbedding(StoreEmbedding storeEmbedding, Store store) {
        Optional<StoreEmbedding> storeEmbeddingOptional = storeEmbeddingRepository.findByStore(store);
        if (storeEmbeddingOptional.isEmpty()) {
            storeEmbeddingRepository.save(storeEmbedding);
            return;
        }

        StoreEmbedding existing = storeEmbeddingOptional.get();
        StoreEmbedding updated = StoreEmbedding.builder()
                .store(existing.getStore())
                .embedding(storeEmbedding.getEmbedding())
                .embeddingStatus(storeEmbedding.getEmbeddingStatus())
                .build();

        existing.update(updated);
    }
}
