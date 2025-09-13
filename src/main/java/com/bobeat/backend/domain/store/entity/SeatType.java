package com.bobeat.backend.domain.store.entity;

public enum SeatType {
    FOR_ONE("1인용"),
    FOR_TWO("2인용"),
    FOR_FOUR("4인용"),
    BAR_TABLE("바 좌석"),
    CUBICLE("칸막");

    private final String name;

    SeatType(String name) {
        this.name = name;
    }
}
