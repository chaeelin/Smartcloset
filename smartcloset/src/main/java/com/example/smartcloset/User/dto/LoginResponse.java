package com.example.smartcloset.User.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String token;
    private String loginId;
    private String nickname;

    public LoginResponse(String token, String loginId, String nickname) {
        this.token = token;
        this.loginId = loginId;
        this.nickname = nickname;
    }

}
