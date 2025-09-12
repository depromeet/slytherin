package com.bobeat.backend.domain.store.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSeatCategory is a Querydsl query type for SeatCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSeatCategory extends EntityPathBase<SeatCategory> {

    private static final long serialVersionUID = 695805679L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSeatCategory seatCategory = new QSeatCategory("seatCategory");

    public final com.bobeat.backend.domain.common.QBaseTimeEntity _super = new com.bobeat.backend.domain.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<SeatType> seatType = createEnum("seatType", SeatType.class);

    public final QStore store;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSeatCategory(String variable) {
        this(SeatCategory.class, forVariable(variable), INITS);
    }

    public QSeatCategory(Path<? extends SeatCategory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSeatCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSeatCategory(PathMetadata metadata, PathInits inits) {
        this(SeatCategory.class, metadata, inits);
    }

    public QSeatCategory(Class<? extends SeatCategory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new QStore(forProperty("store"), inits.get("store")) : null;
    }

}

