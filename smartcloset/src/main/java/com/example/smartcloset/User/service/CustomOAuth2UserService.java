package com.example.smartcloset.User.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 네이버의 사용자 정보는 'response' 키에 포함되어 있습니다.
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        // 회원 정보 추출
        String nickname = (String) response.get("nickname");        // 별명
        String profileImage = (String) response.get("profile_image"); // 프로필 사진
        String gender = (String) response.get("gender");            // 성별

        // 여기서 원하는 대로 사용자 정보를 처리할 수 있습니다.
        System.out.println("Nickname: " + nickname);
        System.out.println("Profile Image: " + profileImage);
        System.out.println("Gender: " + gender);

        return oAuth2User;
    }
}