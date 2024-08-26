package com.example.smartcloset.User.dto;

import com.example.smartcloset.User.entity.Gender;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoProfile {

    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakao_account;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {

        @JsonProperty("profile")
        private KakaoProfileProperties profile;

//        @JsonProperty("gender")
//        private String gender;
    }

    @Getter
    @Setter
    public static class KakaoProfileProperties {

        @JsonProperty("nickname")
        private String nickname;

        @JsonProperty("profile_image")
        private String profile_image;
    }
}