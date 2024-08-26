package com.example.smartcloset.User.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class KakaoResponse {
    private String token;
    private String kakaoId;
    private String nickname;
    private String profilePicture;
}
