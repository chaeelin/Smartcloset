package com.example.smartcloset.User.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -76966355L;

    public static final QUser user = new QUser("user");

    public final DateTimePath<java.sql.Timestamp> date = createDateTime("date", java.sql.Timestamp.class);

    public final EnumPath<Gender> gender = createEnum("gender", Gender.class);

    public final NumberPath<Integer> height = createNumber("height", Integer.class);

    public final StringPath kakaoId = createString("kakaoId");

    public final StringPath loginId = createString("loginId");

    public final StringPath loginPwd = createString("loginPwd");

    public final StringPath nickname = createString("nickname");

    public final EnumPath<Platform> platform = createEnum("platform", Platform.class);

    public final ListPath<com.example.smartcloset.board.model.Post, com.example.smartcloset.board.model.QPost> posts = this.<com.example.smartcloset.board.model.Post, com.example.smartcloset.board.model.QPost>createList("posts", com.example.smartcloset.board.model.Post.class, com.example.smartcloset.board.model.QPost.class, PathInits.DIRECT2);

    public final StringPath profilePicture = createString("profilePicture");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final NumberPath<Integer> weight = createNumber("weight", Integer.class);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

