package com.bobeat.backend.domain.store.external.kakao.service;

import com.bobeat.backend.domain.store.external.kakao.dto.KakaoMenuDto;
import com.bobeat.backend.domain.store.external.kakao.dto.KakaoStoreDto;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CrawlerService {

    private WebDriver driver;

    public CrawlerService() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--disable-gpu", "--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/114.0.5735.110 Safari/537.36");
        driver = new ChromeDriver(options);
    }

    public KakaoStoreDto crawlingKakaoMap(String storeId) {
        try {
            String url = "https://place.map.kakao.com/" + storeId;
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            String name = parseStoreName(wait);
            String address = parseStoreAddress(wait);
            List<String> imageUrls = parseStoreImageUrls(wait);
            List<KakaoMenuDto> menuDtos = parseStoreMenus(wait);
            String phoneNumber = parsePhoneName(wait);

            return KakaoStoreDto.builder()
                    .name(name)
                    .address(address)
                    .phoneNumber(phoneNumber)
                    .imageUrls(imageUrls)
                    .menuDtos(menuDtos)
                    .build();
        } finally {
            driver.quit();
        }
    }

    private String parseStoreName(WebDriverWait wait) {
        try {
            WebElement nameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("#mainContent > div.top_basic > div.info_main > div.unit_info > h3")
            ));
            String name = nameElement.getText();
            log.info("가게 제목 조회: {}", name);
            return name;
        } catch (Exception e) {
            log.info("가게 제목 조회 실패");
            return null;
        }
    }

    private String parseStoreAddress(WebDriverWait wait) {
        WebElement addressElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(
                        "#mainContent > div.main_detail.home > div.detail_cont > div.section_comm.section_defaultinfo > div > div:nth-child(3) > div > div:nth-child(1) > span")
        ));
        return addressElement.getText();
    }

    private List<String> parseStoreImageUrls(WebDriverWait wait) {
        List<String> result = new ArrayList<>();
        try {
            WebElement ulElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(
                            "#mainContent > div.main_detail.home > div.detail_cont > div.section_comm.section_review > div.group_total > div.wrap_photo.scroll_start > div > ul"))
            );

            List<WebElement> liElements = ulElement.findElements(By.tagName("li"));

            for (WebElement li : liElements) {
                List<WebElement> imgElements = li.findElements(By.tagName("img"));
                log.info("가게 이미지 조회");
                List<String> imgSrcs = imgElements.stream()
                        .map(webElement -> webElement.getAttribute("src"))
                        .toList();
                result.addAll(imgSrcs);
            }
        } catch (Exception e) {
            log.info("가게 이미지 실패");
            return result;
        }
        return result;
    }

    private List<KakaoMenuDto> parseStoreMenus(WebDriverWait wait) {
        List<KakaoMenuDto> result = new ArrayList<>();

        try {
            WebElement ulElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(
                            "#mainContent > div.main_detail.home > div.detail_cont > div.section_comm.section_product > div.wrap_goods > ul")
            ));

            List<WebElement> liElements = ulElement.findElements(By.tagName("li"));

            for (WebElement li : liElements) {
                WebElement imgElement = null;
                WebElement nameElement = null;
                WebElement priceElement = null;

                try {
                    imgElement = li.findElement(By.tagName("img"));
                } catch (Exception ignored) {
                }
                try {
                    nameElement = li.findElement(By.className("tit_item"));
                } catch (Exception ignored) {
                }

                try {
                    priceElement = li.findElement(By.className("desc_item"));
                } catch (Exception ignored) {
                }

                String name = null;
                if (nameElement != null) {
                    name = nameElement.getText();
                }

                Long price = null;
                if (priceElement != null) {
                    price = convertStringToInt(priceElement.getText());

                }

                String imageUrl = null;
                if (priceElement != null) {
                    imageUrl = imgElement.getAttribute("src");
                }
                result.add(KakaoMenuDto.builder()
                        .name(name)
                        .price(price)
                        .imageUrl(imageUrl)
                        .build());
            }

        } catch (Exception e) {
            log.info("메뉴 크롤링 실패");
            return result;
        }
        return result;
    }

    private String parsePhoneName(WebDriverWait wait) {
        try {
            WebElement nameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(
                            "#mainContent > div.main_detail.home > div.detail_cont > div.section_comm.section_defaultinfo > div > div:nth-child(4) > div > div > span")
            ));
            String phoneName = nameElement.getText();
            log.info("가게 전화번호 조회: {}", phoneName);
            return phoneName;
        } catch (Exception e) {
            log.info("가게 전화번호 조회 실패");
            return null;
        }
    }

    private Long convertStringToInt(String price) {
        String cleaned = price.replaceAll("[^0-9]", "");

        return Long.valueOf(cleaned);
    }
}

