package com.example.smartcloset.board.model;

import com.example.smartcloset.comment.entity.CommentEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private int likes;
    private LocalDateTime date;
    private String imageUrl;
    private int commentsCount;

    // 댓글과의 연관 관계 설정
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments;  // 댓글 리스트 추가
}