package com.bobeat.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ncp.clova.host}")
    private String clovaHost;

    @Value("${ncp.clova.api-key-secret}")
    private String apiKeySecret;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    public WebClient clovaWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(clovaHost)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKeySecret)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}