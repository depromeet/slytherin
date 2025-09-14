package com.bobeat.backend.domain.store.entity;

import com.bobeat.backend.domain.common.BaseTimeEntity;
import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.domain.store.vo.Categories;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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

    private Level honbobLevel;

    @Embedded
    private Categories categories;
}
