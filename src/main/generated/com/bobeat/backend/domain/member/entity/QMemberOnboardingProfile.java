package com.bobeat.backend.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberOnboardingProfile is a Querydsl query type for MemberOnboardingProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberOnboardingProfile extends EntityPathBase<MemberOnboardingProfile> {

    private static final long serialVersionUID = -1709993909L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberOnboardingProfile memberOnboardingProfile = new QMemberOnboardingProfile("memberOnboardingProfile");

    public final com.bobeat.backend.domain.common.QBaseTimeEntity _super = new com.bobeat.backend.domain.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<Level> honbapLevel = createEnum("honbapLevel", Level.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMember member;

    public final EnumPath<OnboardingAnswer> question1 = createEnum("question1", OnboardingAnswer.class);

    public final EnumPath<OnboardingAnswer> question2 = createEnum("question2", OnboardingAnswer.class);

    public final EnumPath<OnboardingAnswer> question3 = createEnum("question3", OnboardingAnswer.class);

    public final EnumPath<OnboardingAnswer> question4 = createEnum("question4", OnboardingAnswer.class);

    public final EnumPath<OnboardingAnswer> question5 = createEnum("question5", OnboardingAnswer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMemberOnboardingProfile(String variable) {
        this(MemberOnboardingProfile.class, forVariable(variable), INITS);
    }

    public QMemberOnboardingProfile(Path<? extends MemberOnboardingProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberOnboardingProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberOnboardingProfile(PathMetadata metadata, PathInits inits) {
        this(MemberOnboardingProfile.class, metadata, inits);
    }

    public QMemberOnboardingProfile(Class<? extends MemberOnboardingProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member"), inits.get("member")) : null;
    }

}

