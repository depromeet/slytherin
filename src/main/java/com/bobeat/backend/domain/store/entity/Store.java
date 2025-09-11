package com.bobeat.backend.domain.store.entity;

import com.bobeat.backend.domain.common.BaseTimeEntity;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.domain.store.vo.Categories;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    private Address address;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private StoreType storeType;

    private String description;

    private String mainImageUrl;

    private Integer honbobLevel;

    private int turnoverMinute;

    @Embedded
    private Categories categories;
}
