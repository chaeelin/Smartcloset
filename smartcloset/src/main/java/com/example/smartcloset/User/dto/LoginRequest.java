package com.example.smartcloset.User.dto;

import lombok.*;

@Getter
@Setter
public class LoginRequest {
    private String loginId;
    private String loginPwd;

    public LoginRequest(String loginId, String loginPwd) {
        this.loginId = loginId;
        this.loginPwd = loginPwd;
    }
}
