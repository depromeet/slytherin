package com.bobeat.backend.domain.store.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSecondaryCategory is a Querydsl query type for SecondaryCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSecondaryCategory extends EntityPathBase<SecondaryCategory> {

    private static final long serialVersionUID = 577302854L;

    public static final QSecondaryCategory secondaryCategory = new QSecondaryCategory("secondaryCategory");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath secondaryType = createString("secondaryType");

    public QSecondaryCategory(String variable) {
        super(SecondaryCategory.class, forVariable(variable));
    }

    public QSecondaryCategory(Path<? extends SecondaryCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSecondaryCategory(PathMetadata metadata) {
        super(SecondaryCategory.class, metadata);
    }

}

