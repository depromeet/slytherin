package com.bobeat.backend.domain.store.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPrimaryCategory is a Querydsl query type for PrimaryCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPrimaryCategory extends EntityPathBase<PrimaryCategory> {

    private static final long serialVersionUID = -2113389356L;

    public static final QPrimaryCategory primaryCategory = new QPrimaryCategory("primaryCategory");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath primaryType = createString("primaryType");

    public QPrimaryCategory(String variable) {
        super(PrimaryCategory.class, forVariable(variable));
    }

    public QPrimaryCategory(Path<? extends PrimaryCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPrimaryCategory(PathMetadata metadata) {
        super(PrimaryCategory.class, metadata);
    }

}

