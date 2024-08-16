package com.example.smartcloset.User.dto;

import com.example.smartcloset.User.entity.Gender;
import com.example.smartcloset.User.entity.Platform;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserRequest {
    private String loginId;
    private String loginPwd;
    private String nickname;
    private int height;
    private int weight;
    private Platform platform;  // Platform enum 사용
    private Gender gender; // 성별 필드 추가

    // 기본 생성자 추가 (필수)
    public UserRequest() {
    }

    // 키와 몸무게 수정
    public UserRequest(int height, int weight) {
        this.height = height;
        this.weight = weight;
    }

    // 로그인
    public UserRequest(String loginId, String loginPwd) {
        this.loginId = loginId;
        this.loginPwd = loginPwd;
    }
}
