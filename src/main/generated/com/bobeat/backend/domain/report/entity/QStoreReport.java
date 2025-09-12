package com.bobeat.backend.domain.report.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreReport is a Querydsl query type for StoreReport
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreReport extends EntityPathBase<StoreReport> {

    private static final long serialVersionUID = 877658066L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreReport storeReport = new QStoreReport("storeReport");

    public final com.bobeat.backend.domain.common.QBaseTimeEntity _super = new com.bobeat.backend.domain.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath location = createString("location");

    public final com.bobeat.backend.domain.member.entity.QMember member;

    public final ListPath<String, StringPath> menuCategories = this.<String, StringPath>createList("menuCategories", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final ListPath<String, StringPath> paymentMethods = this.<String, StringPath>createList("paymentMethods", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath reason = createString("reason");

    public final StringPath recommendedMenu = createString("recommendedMenu");

    public final ListPath<com.bobeat.backend.domain.store.entity.SeatType, EnumPath<com.bobeat.backend.domain.store.entity.SeatType>> seatTypes = this.<com.bobeat.backend.domain.store.entity.SeatType, EnumPath<com.bobeat.backend.domain.store.entity.SeatType>>createList("seatTypes", com.bobeat.backend.domain.store.entity.SeatType.class, EnumPath.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QStoreReport(String variable) {
        this(StoreReport.class, forVariable(variable), INITS);
    }

    public QStoreReport(Path<? extends StoreReport> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreReport(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreReport(PathMetadata metadata, PathInits inits) {
        this(StoreReport.class, metadata, inits);
    }

    public QStoreReport(Class<? extends StoreReport> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.bobeat.backend.domain.member.entity.QMember(forProperty("member"), inits.get("member")) : null;
    }

}

