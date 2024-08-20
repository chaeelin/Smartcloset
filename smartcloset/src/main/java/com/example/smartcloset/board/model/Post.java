package com.example.smartcloset.board.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Post {
    private Long id;
    private String title;
    private String content;
    private int likes;
    private LocalDateTime date;
    private String imageUrl;
    private int commentsCount;
}