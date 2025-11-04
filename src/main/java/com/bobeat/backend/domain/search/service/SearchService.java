package com.bobeat.backend.domain.search.service;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.search.entity.SearchHistory;
import com.bobeat.backend.domain.search.repository.SearchHistoryRepository;
import com.bobeat.backend.domain.store.dto.response.StoreSearchResultDto;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreImage;
import com.bobeat.backend.domain.store.repository.StoreImageRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import com.bobeat.backend.domain.store.service.StoreService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public List<StoreSearchResultDto> searchStore(Long userId, String query) {

        List<Store> stores = storeRepository.findAll().stream()
                .limit(5)
                .toList();

        List<Long> storeIds = stores.stream()
                .map(Store::getId)
                .toList();
        Map<Long, List<String>> seatTypes = storeRepository.findSeatTypes(storeIds);

        List<StoreSearchResultDto> storeSearchResultDtos = stores.stream()
                .map(store -> {
                    StoreImage storeimage = storeImageRepository.findByStoreAndIsMainTrue(store);
                    List<String> seatTypeNames = seatTypes.getOrDefault(store.getId(), List.of());
                    List<String> tagNames = storeService.buildTagsFromCategories(store.getCategories());
                    return StoreSearchResultDto.of(store, storeimage, seatTypeNames, tagNames);
                })
                .toList();
        saveSearchHistory(userId, query);

        return storeSearchResultDtos;
    }


    @Transactional
    public void saveSearchHistory(Long memberId, String query) {
        Member member = memberRepository.findByMemberId(memberId);
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
    }
}
