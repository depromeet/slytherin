package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.store.dto.response.KakaoStoreResponse;
import com.bobeat.backend.domain.store.external.kakao.dto.KakaoDocument;
import com.bobeat.backend.domain.store.external.kakao.dto.KakaoStoreDto;
import com.bobeat.backend.domain.store.external.kakao.service.CrawlerService;
import com.bobeat.backend.domain.store.external.kakao.service.KakaoSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreCrawlingService {

    private final KakaoSearchService kakaoSearchService;
    private final CrawlerService crawlerService;


    public KakaoStoreResponse findStore(String storeName) {
        KakaoDocument kakaoDocument = kakaoSearchService.searchAddress(storeName);
        KakaoStoreDto kakaoStoreDto = crawlerService.crawlingKakaoMap(kakaoDocument.id());
        return KakaoStoreResponse.of(kakaoDocument, kakaoStoreDto);
    }
}
