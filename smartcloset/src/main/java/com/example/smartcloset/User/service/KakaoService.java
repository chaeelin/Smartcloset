package com.example.smartcloset.User.service;

import com.example.smartcloset.User.dto.KakaoProfile;
import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.User.repository.UserRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;


@Service
public class KakaoService {

    private static final String KAKAO_USERINFO_URL = "https://kapi.kakao.com/v2/user/me";  // 카카오 사용자 정보 요청 URL
    private final UserRepository userRepository; // UserRepository 인스턴스

    @Autowired
    public KakaoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public KakaoProfile getKakaoProfile(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<KakaoProfile> response = restTemplate.exchange(
                KAKAO_USERINFO_URL,
                HttpMethod.GET,
                entity,
                KakaoProfile.class
        );

        return response.getBody();
    }

    // 카카오 사용자 정보를 기반으로 회원을 생성하고 저장하는 메서드
    public User processKakaoLogin(KakaoProfile kakaoProfile) {
        String kakaoId = String.valueOf(kakaoProfile.getId());

        // 카카오 ID로 기존 사용자가 있는지 확인
        return userRepository.findByKakaoId(kakaoId)  // 인스턴스 메서드로 호출
                .orElseGet(() -> {
                    // 존재하지 않으면 새로운 회원 생성
                    User newMember = new User();
                    newMember.setKakaoId(kakaoId);
                    newMember.setNickname(kakaoProfile.getKakaoAccount().getProfile().getNickname());
                    newMember.setGender(kakaoProfile.getKakaoAccount().getGender());
                    newMember.setProfilePicture(kakaoProfile.getKakaoAccount().getProfile().getProfileImageUrl());
                    return userRepository.save(newMember);
                });
    }
}
