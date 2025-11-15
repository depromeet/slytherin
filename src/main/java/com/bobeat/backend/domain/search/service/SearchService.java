package com.bobeat.backend.domain.search.service;

import static com.bobeat.backend.global.exception.ErrorCode.INTERNAL_SERVER;
import static com.bobeat.backend.global.exception.ErrorCode.SEARCH_HISTORY_ACCESS_DENIED;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.search.dto.request.StoreSearchRequest;
import com.bobeat.backend.domain.search.dto.response.StoreSearchHistoryResponse;
import com.bobeat.backend.domain.search.entity.SearchHistory;
import com.bobeat.backend.domain.search.entity.SearchHistoryEmbedding;
import com.bobeat.backend.domain.search.repository.SearchHistoryEmbeddingRepository;
import com.bobeat.backend.domain.search.repository.SearchHistoryRepository;
import com.bobeat.backend.domain.store.dto.StoreEmbeddingWithDistanceDto;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto.Coordinate;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto.SignatureMenu;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreEmbedding;
import com.bobeat.backend.domain.store.entity.StoreImage;
import com.bobeat.backend.domain.store.external.clova.service.ClovaEmbeddingClient;
import com.bobeat.backend.domain.store.repository.SeatOptionRepository;
import com.bobeat.backend.domain.store.repository.StoreEmbeddingQueryRepository;
import com.bobeat.backend.domain.store.repository.StoreImageRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.domain.store.service.StoreService;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.request.CursorPaginationRequest;
import com.bobeat.backend.global.response.CursorPageResponse;
import com.bobeat.backend.global.util.KeysetCursor;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;
    private final StoreService storeService;
    private final SearchHistoryRepository searchHistoryRepository;
    private final MemberRepository memberRepository;
    private final ClovaEmbeddingClient clovaEmbeddingClient;
    private final StoreEmbeddingQueryRepository storeEmbeddingQueryRepository;
    private final SeatOptionRepository seatOptionRepository;
    private final SearchHistoryEmbeddingRepository searchHistoryEmbeddingRepository;

    public CursorPageResponse<StoreSearchResultDto> searchStore(StoreSearchRequest request) {
        List<Float> embedding = CheckAndSaveQueryEmbedding(request.query());
        Float lastKnown = null;
        if (request.paging().lastKnown() != null) {
            lastKnown = Float.valueOf(request.paging().lastKnown());
        }
//        List<StoreEmbedding> storeEmbeddings = storeEmbeddingQueryRepository.findSimilarEmbeddingsWithCursor(embedding,
//                lastKnown, request.paging().limit() + 1);
        List<StoreEmbeddingWithDistanceDto> storeEmbeddingWithDistanceDtos = storeEmbeddingQueryRepository.findSimilarEmbeddingsWithCursor(
                embedding, lastKnown, request.paging().limit() + 1,
                request.lon(), request.lat());
        boolean hasNext = checkHasNext(storeEmbeddingWithDistanceDtos, request.paging().limit());

        List<StoreEmbeddingWithDistanceDto> actualStoreEmbeddings = storeEmbeddingWithDistanceDtos.stream()
                .limit(request.paging().limit())
                .toList();
        String nextCursor = findNextCursor(storeEmbeddingWithDistanceDtos, embedding);

        List<Store> stores = actualStoreEmbeddings.stream()
                .map(StoreEmbeddingWithDistanceDto::getEmbedding)
                .map(StoreEmbedding::getStore)
                .toList();

        List<Long> storeIds = stores.stream()
                .map(Store::getId)
                .toList();
        Map<Long, SignatureMenu> repMenus = storeRepository.findRepresentativeMenus(storeIds);
        Map<Long, List<String>> seatTypes = storeRepository.findSeatTypes(storeIds);

        // N+1 쿼리 해결: StoreImage를 배치 조회
        Map<Long, StoreImage> storeImageMap = storeImageRepository.findMainImagesByStoreIds(storeIds).stream()
                .collect(Collectors.toMap(img -> img.getStore().getId(), img -> img));

        List<StoreSearchResultDto> storeSearchResultDtos = actualStoreEmbeddings.stream()
                .map(actualStoreEmbedding -> {
                            Store store = actualStoreEmbedding.getEmbedding().getStore();
                            StoreImage storeImage = storeImageMap.get(store.getId());
                            SignatureMenu signatureMenu = repMenus.get(store.getId());
                            Coordinate coordinate = new Coordinate(store.getAddress().getLatitude(),
                                    store.getAddress().getLongitude());
                            List<String> seatTypeStrings = seatTypes.get(store.getId());

                            List<String> categoryStrings = storeService.buildTagsFromCategories(store.getCategories());

                            int distance = actualStoreEmbedding.getDistance();
                            int walkingMinutes = (int) Math.ceil(distance / 80.0);
                            return new StoreSearchResultDto(store.getId(), store.getName(),
                                    storeImage != null ? storeImage.getImageUrl() : null,
                                    signatureMenu, coordinate, distance, walkingMinutes, seatTypeStrings, categoryStrings,
                                    store.getHonbobLevel() != null ? store.getHonbobLevel().getValue() : 0);
                        }
                )
                .toList();

        return new CursorPageResponse<>(storeSearchResultDtos, nextCursor, hasNext, null);
    }

    public CursorPageResponse<StoreSearchResultDto> searchStoreWithMember(Long memberId,
                                                                          StoreSearchRequest request) {
        CursorPageResponse<StoreSearchResultDto> response = searchStore(request);
        saveSearchHistory(memberId, request.query());
        return response;
    }

    @Transactional
    public void saveSearchHistory(Long memberId, String query) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        Optional<SearchHistory> searchHistoryOptional = searchHistoryRepository.findByQueryAndMember(query, member);

        if (searchHistoryOptional.isEmpty()) {
            SearchHistory searchHistory = SearchHistory.builder()
                    .query(query)
                    .member(member)
                    .build();
            searchHistoryRepository.save(searchHistory);
            return;
        }
        SearchHistory searchHistory = searchHistoryOptional.get();
        searchHistory.updateUpdatedAt();
        searchHistoryRepository.saveAndFlush(searchHistory);
    }


    public List<StoreSearchHistoryResponse> getStoreSearchHistory(Long memberId, CursorPaginationRequest paging) {
        List<SearchHistory> searchHistories = searchHistoryRepository.findByMemberWithCursor(memberId,
                paging.lastKnown(), paging.limit());

        return searchHistories.stream()
                .map(StoreSearchHistoryResponse::from)
                .toList();
    }

    @Transactional
    public void deleteStoreSearchHistory(Long memberId) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        searchHistoryRepository.deleteByMember(member);
    }

    @Transactional
    public void deleteStoreSearchHistoryById(Long memberId, Long id) {
        SearchHistory searchHistory = searchHistoryRepository.findBySearchHistoryId(id);
        Long findMemberId = searchHistory.getMember().getId();

        if (!findMemberId.equals(memberId)) {
            throw new CustomException(SEARCH_HISTORY_ACCESS_DENIED);
        }
        searchHistoryRepository.delete(searchHistory);
    }

    @Transactional
    public List<Float> CheckAndSaveQueryEmbedding(String query) {
        SearchHistoryEmbedding searchHistoryEmbeddings = searchHistoryEmbeddingRepository.findByQuery(query);
        if (searchHistoryEmbeddings != null) {
            float[] embedding = searchHistoryEmbeddings.getEmbedding();
            return IntStream.range(0, embedding.length)
                    .mapToObj(i -> embedding[i])
                    .collect(Collectors.toList());
        }
        List<Float> embedding = clovaEmbeddingClient.getEmbeddingSync(query);

        float[] embeddingArray = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            embeddingArray[i] = embedding.get(i);
        }

        SearchHistoryEmbedding searchHistoryEmbedding = SearchHistoryEmbedding.builder()
                .embedding(embeddingArray)
                .query(query)
                .build();
        searchHistoryEmbeddingRepository.save(searchHistoryEmbedding);
        return embedding;
    }

    private CursorPageResponse<StoreSearchResultDto> buildStoreSearchResponse(List<Store> stores,
                                                                              CursorPaginationRequest paging) {
        final int temporaryDistance = 20;
        final int temporaryWorkingDistance = 20;

        final int pageSize = paging != null ? paging.limit() : 20;

        boolean hasNext = stores.size() > paging.limit();

        List<Long> storeIds = stores.stream()
                .map(Store::getId)
                .toList();

        var repMenuMap = storeRepository.findRepresentativeMenus(storeIds);
        var seatTypesMap = storeRepository.findSeatTypes(storeIds);

        // N+1 쿼리 해결: StoreImage를 배치 조회
        Map<Long, StoreImage> storeImageMap = storeImageRepository.findMainImagesByStoreIds(storeIds).stream()
                .collect(Collectors.toMap(img -> img.getStore().getId(), img -> img));

        List<StoreSearchResultDto> data = stores.stream()
                .map(store -> {
                    var mainImage = storeImageMap.get(store.getId());

                    long id = store.getId();

                    return new StoreSearchResultDto(
                            store.getId(),
                            store.getName(),
                            mainImage != null ? mainImage.getImageUrl() : null,
                            repMenuMap.getOrDefault(id, new StoreSearchResultDto.SignatureMenu(null, 0)),
                            new StoreSearchResultDto.Coordinate(store.getAddress().getLatitude(),
                                    store.getAddress().getLongitude()),
                            temporaryDistance,
                            temporaryWorkingDistance,
                            seatTypesMap.getOrDefault(id, List.of()),
                            storeService.buildTagsFromCategories(store.getCategories()),
                            store.getHonbobLevel() != null ? store.getHonbobLevel().getValue() : 0
                    );
                })
                .collect(Collectors.toList());

        String nextCursor = null;
        if (hasNext && !stores.isEmpty()) {
            Store lastStore = stores.getLast();
            nextCursor = KeysetCursor.encode(temporaryDistance, lastStore.getId());
        }

        return new CursorPageResponse<>(data, nextCursor, hasNext, null);
    }

    private boolean checkHasNext(List<StoreEmbeddingWithDistanceDto> storeEmbeddings, @NotNull int limit) {
        if (storeEmbeddings.size() > limit) {
            return true;
        }
        return false;
    }

    private String findNextCursor(List<StoreEmbeddingWithDistanceDto> storeEmbeddings, List<Float> compareEmbedding) {
        if (storeEmbeddings.isEmpty()) {
            throw new CustomException("마지막 인덱스입니다.", INTERNAL_SERVER);
        }

        StoreEmbeddingWithDistanceDto storeEmbedding = storeEmbeddings.get(storeEmbeddings.size() - 1);
        float[] storeVector = storeEmbedding.embedding();

        float dot = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;

        for (int i = 0; i < compareEmbedding.size(); i++) {
            dot += compareEmbedding.get(i) * storeVector[i];
            normA += compareEmbedding.get(i) * compareEmbedding.get(i);
            normB += storeVector[i] * storeVector[i];
        }

        double cosineSimilarity = dot / (Math.sqrt(normA) * Math.sqrt(normB));
        double distance = 1 - cosineSimilarity;
        return String.valueOf(distance);
    }
}
