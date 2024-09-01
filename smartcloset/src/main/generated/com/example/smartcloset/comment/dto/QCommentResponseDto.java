package com.example.smartcloset.comment.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.example.smartcloset.comment.dto.QCommentResponseDto is a Querydsl Projection type for CommentResponseDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QCommentResponseDto extends ConstructorExpression<CommentResponseDto> {

    private static final long serialVersionUID = -1849628137L;

    public QCommentResponseDto(com.querydsl.core.types.Expression<Long> commentId, com.querydsl.core.types.Expression<String> content, com.querydsl.core.types.Expression<Integer> reportCount, com.querydsl.core.types.Expression<Long> parentId, com.querydsl.core.types.Expression<String> userName) {
        super(CommentResponseDto.class, new Class<?>[]{long.class, String.class, int.class, long.class, String.class}, commentId, content, reportCount, parentId, userName);
    }

}

