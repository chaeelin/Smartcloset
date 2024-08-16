package com.example.smartcloset.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private int likes;
    private LocalDateTime date;
    private String imageUrl;
    private int commentsCount;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }
}
