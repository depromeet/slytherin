package com.bobeat.backend.domain.store.external.kakao;

import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SeleniumDriver {
    private WebDriver driver;

    public SeleniumDriver() {
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            this.driver = new RemoteWebDriver(new URL("http://selenium:4444/wd/hub"), options);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public WebDriver getDriver() {
        return driver;
    }
}