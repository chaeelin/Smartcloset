package com.example.smartcloset.entity;

import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.model.Post;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommentEntity parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
