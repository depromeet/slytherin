package com.bobeat.backend.domain.store.entity;

import lombok.Getter;

@Getter
public enum ReportType {
    STORE_CLOSED("식당 페업"),
    STORE_INFO("식당 정보"),
    STORE_PHONE_NUMBER("전화 번호"),
    STORE_LOCATION("위치 정보"),
    STORE_BUSINESS_HOURS("영업 시간");

    private final String content;

    ReportType(String content) {
        this.content = content;
    }
}
