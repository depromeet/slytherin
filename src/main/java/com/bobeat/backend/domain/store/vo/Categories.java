package com.bobeat.backend.domain.store.vo;

import com.bobeat.backend.domain.store.entity.PrimaryCategory;
import com.bobeat.backend.domain.store.entity.SecondaryCategory;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Categories {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_category_id")
    private PrimaryCategory primaryCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_category_id")
    private SecondaryCategory secondaryCategory;
}
