package com.bobeat.backend.domain.store.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSeatOption is a Querydsl query type for SeatOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSeatOption extends EntityPathBase<SeatOption> {

    private static final long serialVersionUID = 268745382L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSeatOption seatOption = new QSeatOption("seatOption");

    public final com.bobeat.backend.domain.common.QBaseTimeEntity _super = new com.bobeat.backend.domain.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final NumberPath<Integer> maxCapacity = createNumber("maxCapacity", Integer.class);

    public final EnumPath<SeatType> seatType = createEnum("seatType", SeatType.class);

    public final QStore store;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSeatOption(String variable) {
        this(SeatOption.class, forVariable(variable), INITS);
    }

    public QSeatOption(Path<? extends SeatOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSeatOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSeatOption(PathMetadata metadata, PathInits inits) {
        this(SeatOption.class, metadata, inits);
    }

    public QSeatOption(Class<? extends SeatOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new QStore(forProperty("store"), inits.get("store")) : null;
    }

}

