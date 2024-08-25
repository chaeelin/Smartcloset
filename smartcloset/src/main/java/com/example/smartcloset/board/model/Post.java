package com.example.smartcloset.board.model;

import com.example.smartcloset.User.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY) // Lazy Loading으로 변경하여 필요할 때만 로드되도록 설정
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 댓글과의 연관 관계 설정
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments;  // 댓글 리스트 추가

    // 기본 생성자
    public Post() {
        // 기본 생성자 필수
    }

    // 사용자 필드를 포함한 생성자 추가
    public Post(String title, String content, LocalDateTime date, User user) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.user = user; // 사용자 필드 반드시 설정
        this.likes = 0;
        this.commentsCount = 0;
    }

    // 사용자 필드가 설정되지 않았을 경우 예외를 발생시키는 메서드
    @PrePersist
    @PreUpdate
    private void validateUser() {
        if (this.user == null) {
            throw new IllegalStateException("User field cannot be null when saving Post entity");
        }
    }
}
