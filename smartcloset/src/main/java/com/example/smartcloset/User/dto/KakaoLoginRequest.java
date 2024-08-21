package com.example.smartcloset.User.dto;

public class KakaoLoginRequest {

    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String toString() {
        return "KakaoLoginRequest{" +
                "accessToken='" + accessToken + '\'' +
                '}';
    }
}
