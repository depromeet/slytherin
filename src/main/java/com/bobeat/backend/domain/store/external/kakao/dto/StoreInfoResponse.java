package com.bobeat.backend.domain.store.external.kakao.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreInfoResponse {
    private String name;
    private Address address;
    private String phoneNumber;
    private String description;
    private String mainImageUrl;
    private int honbobLevel;
    private Categories categories;
    private List<StoreImage> storeImages;
    private List<Menu> menus;
    private List<SeatOption> seatOptions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Address {
        private String address;
        private double latitude;
        private double longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Categories {
        private String primaryCategory;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreImage {
        private String imageUrl;
        private boolean isMain;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Menu {
        private String name;
        private int price;
        private String imageUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatOption {
        private String seatType;
        private String imageUrl;
    }
}

