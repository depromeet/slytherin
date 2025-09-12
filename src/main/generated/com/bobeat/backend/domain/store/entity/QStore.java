package com.bobeat.backend.domain.store.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStore is a Querydsl query type for Store
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStore extends EntityPathBase<Store> {

    private static final long serialVersionUID = -1427080811L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStore store = new QStore("store");

    public final com.bobeat.backend.domain.common.QBaseTimeEntity _super = new com.bobeat.backend.domain.common.QBaseTimeEntity(this);

    public final com.bobeat.backend.domain.store.vo.QAddress address;

    public final com.bobeat.backend.domain.store.vo.QCategories categories;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Integer> honbobLevel = createNumber("honbobLevel", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath mainImageUrl = createString("mainImageUrl");

    public final StringPath name = createString("name");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final EnumPath<StoreType> storeType = createEnum("storeType", StoreType.class);

    public final NumberPath<Integer> turnoverMinute = createNumber("turnoverMinute", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QStore(String variable) {
        this(Store.class, forVariable(variable), INITS);
    }

    public QStore(Path<? extends Store> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStore(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStore(PathMetadata metadata, PathInits inits) {
        this(Store.class, metadata, inits);
    }

    public QStore(Class<? extends Store> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.address = inits.isInitialized("address") ? new com.bobeat.backend.domain.store.vo.QAddress(forProperty("address")) : null;
        this.categories = inits.isInitialized("categories") ? new com.bobeat.backend.domain.store.vo.QCategories(forProperty("categories"), inits.get("categories")) : null;
    }

}

