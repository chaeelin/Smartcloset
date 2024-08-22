package com.example.smartcloset.User.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long user_id;

    @Column(name = "loginId", length = 20, nullable = true, unique = true)
    private String loginId;

    @Column(name = "loginPwd", length = 255, nullable = true)
    private String loginPwd;

    @Column(name = "nickname", length = 20, nullable = false, unique = true)
    private String nickname;

    @Column(name = "height")
    private int height;

    @Column(name = "weight")
    private int weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 10, nullable = false)
    private Platform platform;

    @CreationTimestamp
    @Column(name = "date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp date;

    @Enumerated(EnumType.STRING) // 성별 필드 추가
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "profilePicture")
    private String profilePicture;

    @Column(name = "kakaoId")
    private String kakaoId;
}

