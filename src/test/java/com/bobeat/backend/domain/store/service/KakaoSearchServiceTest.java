package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.store.external.kakao.dto.KakaoDocument;
import com.bobeat.backend.domain.store.external.kakao.dto.KakaoStoreDto;
import com.bobeat.backend.domain.store.external.kakao.service.CrawlerService;
import com.bobeat.backend.domain.store.external.kakao.service.KakaoSearchService;
import com.bobeat.backend.global.db.PostgreSQLTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@PostgreSQLTestContainer
public class KakaoSearchServiceTest {

    @Autowired
    KakaoSearchService kakaoSearchService;

    @Autowired
    CrawlerService crawlerService;

    @Test
    void 카카오_API_검증() {
        KakaoDocument kakaoDocument = kakaoSearchService.searchAddress("건대초밥");
        System.out.println("kakaoDocument = " + kakaoDocument);
    }

    @Test
    void 크롤링_테스트1() {
        crawlerService.crawlingKakaoMap("11989881");
    }

    @Test
    void 크롤링_테스트2() {
        crawlerService.crawlingKakaoMap("18542711");
    }

    @Test
    void 크롤링_테스트3() {
        crawlerService.crawlingKakaoMap("27487909");
    }

    @Test
    void 크롤링_테스트4() {
        KakaoStoreDto response = crawlerService.crawlingKakaoMap("27487909");
        System.out.println("response = " + response);
    }
}
