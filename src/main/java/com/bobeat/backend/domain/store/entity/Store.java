package com.bobeat.backend.domain.store.entity;

import com.bobeat.backend.domain.common.BaseTimeEntity;
import com.bobeat.backend.domain.member.entity.Level;
import com.bobeat.backend.domain.store.vo.Address;
import com.bobeat.backend.domain.store.vo.Categories;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store", indexes = {
    @Index(name = "idx_store_internal_score", columnList = "internal_score"),
    @Index(name = "idx_store_honbob_level", columnList = "honbob_level")
})
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

    private String description;

    @Enumerated(EnumType.ORDINAL)
    private Level honbobLevel;

    @Embedded
    private Categories categories;

    @Column(name = "internal_score")
    private Double internalScore;

    public void updateInternalScore(Double score) {
        this.internalScore = score;
    }
}
