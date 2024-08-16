package com.example.smartcloset.User.entity;

public enum Platform {
    APP("app"),
    KAKAO("kakao"),
    NAVER("naver");

    private final String value;

    Platform(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
