package com.example.smartcloset.comment.entity;

import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.board.model.Post;
import com.example.smartcloset.comment.dto.CommentResponseDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comment")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Setter
    @Column(name = "content")
    private String content;

    @Column(name = "report_count")
    @Setter
    private int reportCount;

    // 대댓글 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommentEntity parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public CommentResponseDto toCommentResponseDto(){
        return CommentResponseDto.builder()
                .commentId(this.id).content(this.content)
                .reportCount(this.reportCount)
                .parentId(this.parent.id)
                .userName(this.user.getNickname())
                .build();
    }
}
