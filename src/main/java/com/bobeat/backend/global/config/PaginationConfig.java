package com.bobeat.backend.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 페이지네이션 설정
 * 페이지 크기 제한으로 서버 부하 방지
 */
@Configuration
@ConfigurationProperties(prefix = "pagination")
@Getter
@Setter
public class PaginationConfig {

    /**
     * 기본 페이지 크기
     */
    private Integer defaultPageSize = 20;

    /**
     * 최대 페이지 크기 (보안: 클라이언트가 과도한 limit 요청 방지)
     */
    private Integer maxPageSize = 100;
}