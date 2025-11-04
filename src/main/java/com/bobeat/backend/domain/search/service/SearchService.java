package com.bobeat.backend.domain.search.service;

import static com.bobeat.backend.global.exception.ErrorCode.SEARCH_HISTORY_ACCESS_DENIED;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.search.dto.response.StoreSearchHistoryResponse;
import com.bobeat.backend.domain.search.entity.SearchHistory;
import com.bobeat.backend.domain.search.repository.SearchHistoryRepository;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreEmbedding;
import com.bobeat.backend.domain.store.external.clova.service.ClovaEmbeddingClient;
import com.bobeat.backend.domain.store.repository.StoreEmbeddingQueryRepository;
import com.bobeat.backend.domain.store.repository.StoreImageRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.domain.store.service.StoreService;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.request.CursorPaginationRequest;
import com.bobeat.backend.global.response.CursorPageResponse;
import com.bobeat.backend.global.util.KeysetCursor;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

    public CursorPageResponse<StoreSearchResultDto> searchStoreV2(Long userId, String query,
                                                                  CursorPaginationRequest paging) {

        List<Double> embedding = clovaEmbeddingClient.getEmbeddingSync(query);
        String vectorLiteral = "[" + embedding.stream()
                .map(d -> String.format("%.6f", d))
                .collect(Collectors.joining(",")) + "]";

        List<StoreEmbedding> storeEmbeddings = storeEmbeddingQueryRepository.findSimilarEmbeddingsWithCursor(
                embedding, paging.lastKnown(), paging.limit());
        List<Store> stores = storeEmbeddings.stream()
                .map(StoreEmbedding::getStore)
                .toList();
        saveSearchHistory(userId, query);

        return test(stores, paging);
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

    private CursorPageResponse<StoreSearchResultDto> test(List<Store> stores, CursorPaginationRequest paging) {
        final int temporaryDistance = 20;
        final int temporaryWorkingDistance = 20;

        final int pageSize = paging != null ? paging.limit() : 20;

        boolean hasNext = stores.size() > paging.limit();

        List<Long> storeIds = stores.stream()
                .map(Store::getId)
                .toList();

        var repMenuMap = storeRepository.findRepresentativeMenus(storeIds);
        var seatTypesMap = storeRepository.findSeatTypes(storeIds);

        List<StoreSearchResultDto> data = stores.stream()
                .map(store -> {
                    var mainImage = storeImageRepository.findByStoreAndIsMainTrue(store);

                    long id = store.getId();

                    return new StoreSearchResultDto(
                            store.getId(),
                            store.getName(),
                            mainImage.getImageUrl(),
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
}
