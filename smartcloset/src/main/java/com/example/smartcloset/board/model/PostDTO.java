package com.example.smartcloset.board.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Post 엔티티를 위한 DTO 클래스
@Data
@NoArgsConstructor
@AllArgsConstructor  // 모든 필드를 포함하는 생성자 자동 생성
public class PostDTO {
    private Long id;
    private String title;
    private String content;
    private int likes;
    private LocalDateTime date;
    private String imageUrl;
    private int commentsCount;
    private Long userId;
    private String userName;  // 사용자 이름 추가
    private String userEmail; // 사용자 이메일 추가
    private String loginId;   // 사용자 loginId 추가

    // 필요한 생성자 추가
    public PostDTO(Long id, String title, String content, int likes, LocalDateTime date, String imageUrl, int commentsCount, Long userId, String userName, String userEmail) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.date = date;
        this.imageUrl = imageUrl;
        this.commentsCount = commentsCount;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public PostDTO(Long id, String title, String content, int likes, LocalDateTime date, String imageUrl, int commentsCount, Long userId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.date = date;
        this.imageUrl = imageUrl;
        this.commentsCount = commentsCount;
        this.userId = userId;
    }
}
