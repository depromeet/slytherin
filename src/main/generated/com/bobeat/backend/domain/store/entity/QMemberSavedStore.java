package com.bobeat.backend.domain.store.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberSavedStore is a Querydsl query type for MemberSavedStore
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberSavedStore extends EntityPathBase<MemberSavedStore> {

    private static final long serialVersionUID = 1997458336L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberSavedStore memberSavedStore = new QMemberSavedStore("memberSavedStore");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.bobeat.backend.domain.member.entity.QMember member;

    public final QStore store;

    public QMemberSavedStore(String variable) {
        this(MemberSavedStore.class, forVariable(variable), INITS);
    }

    public QMemberSavedStore(Path<? extends MemberSavedStore> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberSavedStore(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberSavedStore(PathMetadata metadata, PathInits inits) {
        this(MemberSavedStore.class, metadata, inits);
    }

    public QMemberSavedStore(Class<? extends MemberSavedStore> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.bobeat.backend.domain.member.entity.QMember(forProperty("member"), inits.get("member")) : null;
        this.store = inits.isInitialized("store") ? new QStore(forProperty("store"), inits.get("store")) : null;
    }

}

