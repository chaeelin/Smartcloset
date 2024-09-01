package com.example.smartcloset.comment.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentResponseDto {
    private Long commentId;
    private String content;
    private int reportCount;
    private Long parentId;
    private String userName;

    @QueryProjection
    public CommentResponseDto(Long commentId, String content, int reportCount, Long parentId, String userName) {
        this.commentId = commentId;
        this.content = content;
        this.reportCount = reportCount;
        this.parentId = parentId;
        this.userName = userName;
    }
}
