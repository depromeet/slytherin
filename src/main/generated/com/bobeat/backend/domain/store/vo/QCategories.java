package com.bobeat.backend.domain.store.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCategories is a Querydsl query type for Categories
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QCategories extends BeanPath<Categories> {

    private static final long serialVersionUID = 250376658L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCategories categories = new QCategories("categories");

    public final com.bobeat.backend.domain.store.entity.QPrimaryCategory primaryCategory;

    public final com.bobeat.backend.domain.store.entity.QSecondaryCategory secondaryCategory;

    public QCategories(String variable) {
        this(Categories.class, forVariable(variable), INITS);
    }

    public QCategories(Path<? extends Categories> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCategories(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCategories(PathMetadata metadata, PathInits inits) {
        this(Categories.class, metadata, inits);
    }

    public QCategories(Class<? extends Categories> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.primaryCategory = inits.isInitialized("primaryCategory") ? new com.bobeat.backend.domain.store.entity.QPrimaryCategory(forProperty("primaryCategory")) : null;
        this.secondaryCategory = inits.isInitialized("secondaryCategory") ? new com.bobeat.backend.domain.store.entity.QSecondaryCategory(forProperty("secondaryCategory")) : null;
    }

}

