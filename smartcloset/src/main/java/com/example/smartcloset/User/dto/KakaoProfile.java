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
    private KakaoAccount kakaoAccount;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {

        @JsonProperty("gender")
        private Gender gender;

        @JsonProperty("profile")
        private Profile profile;

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {
            private String nickname;
            private String profileImageUrl;
        }
    }
}


//package com.example.smartcloset.User.dto;
//
//import com.example.smartcloset.User.entity.Gender;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Getter;
//import lombok.Setter;
//
//@Getter
//@Setter
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class KakaoProfile {
//
//    private Long id;
//
//    @JsonProperty("kakao_account")
//    private KakaoAccount kakaoAccount;
//
//    @Getter
//    @Setter
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class KakaoAccount {
//
//        @JsonProperty("gender")
//        private Gender gender;  // Gender 타입으로 수정
//
//        @JsonProperty("profile")
//        private Profile profile;
//
//        @Getter
//        @Setter
//        @JsonIgnoreProperties(ignoreUnknown = true)
//        public static class Profile {
//            private String nickname;
//            private String profileImageUrl;
//        }
//    }
//}
