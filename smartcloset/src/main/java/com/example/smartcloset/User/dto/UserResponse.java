package com.example.smartcloset.User.dto;

import com.example.smartcloset.User.entity.Platform;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import com.example.smartcloset.User.entity.Gender;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON으로 변환되지 않음
public class UserResponse {
    private Long userId;
    private String loginId;
    private String nickname;
    private Integer height;
    private Integer weight;
    private Gender gender;
    private String token;
    private Platform platform;

    public UserResponse(Long userId, String loginId, String nickname, Integer height, Integer weight, Gender gender, Platform platform) {
        this.userId = userId;
        this.loginId = loginId;
        this.nickname = nickname;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.platform = platform;
    }

}
