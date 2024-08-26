package com.example.smartcloset.User.service;

import com.example.smartcloset.User.dto.KakaoProfile;
import com.example.smartcloset.User.entity.Platform;
import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.User.repository.UserRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;

@Service
public class KakaoService {

    private static final String KAKAO_USERINFO_URL = "https://kapi.kakao.com/v2/user/me";  // 카카오 사용자 정보 요청 URL
    private final UserRepository userRepository;

    @Autowired
    public KakaoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public KakaoProfile getKakaoProfile(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // Authorization 헤더 설정
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8"); // Content-type 헤더 설정

        HttpEntity<Void> entity = new HttpEntity<>(headers);       // 바디는 설정하지 않고, 헤더만 포함한 HttpEntity 생성

        try {
            ResponseEntity<KakaoProfile> response = restTemplate.exchange(
                    KAKAO_USERINFO_URL,
                    HttpMethod.GET,
                    entity,
                    KakaoProfile.class
            );
            return response.getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to get Kakao profile", e);
        }
    }

    // 추가됨: 사용자가 존재하지 않을 경우 새로운 사용자를 생성하는 메서드
    public User processKakaoLogin(KakaoProfile kakaoProfile) {
        String kakaoId = String.valueOf(kakaoProfile.getId());

        // 카카오 ID로 기존 사용자가 있는지 확인
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> createUserFromKakaoProfile(kakaoProfile)); // 추가됨: 사용자가 없을 경우 새로 생성

        return user;
    }

    // 추가됨: 카카오 프로필 정보를 기반으로 새로운 사용자를 생성하는 메서드
    private User createUserFromKakaoProfile(KakaoProfile kakaoProfile) {
        User newUser = User.builder()
                .kakaoId(String.valueOf(kakaoProfile.getId()))
                .nickname(kakaoProfile.getKakaoAccount().getProfile().getNickname())
                .gender(kakaoProfile.getKakaoAccount().getGender())
                .profilePicture(kakaoProfile.getKakaoAccount().getProfile().getProfileImageUrl())
                .platform(Platform.KAKAO)  // 예시로 플랫폼을 카카오로 지정
                .build();

        return userRepository.save(newUser);
    }
}


//package com.example.smartcloset.User.service;
//
//import com.example.smartcloset.User.dto.KakaoProfile;
//import com.example.smartcloset.User.entity.User;
//import com.example.smartcloset.User.repository.UserRepository;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//
//
//@Service
//public class KakaoService {
//
//    private static final String KAKAO_USERINFO_URL = "https://kapi.kakao.com/v2/user/me";  // 카카오 사용자 정보 요청 URL
//    private final UserRepository userRepository; // UserRepository 인스턴스
//
//    @Autowired
//    public KakaoService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    public KakaoProfile getKakaoProfile(String accessToken) {
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(accessToken);
//        HttpEntity<String> entity = new HttpEntity<>("", headers);
//
//        ResponseEntity<KakaoProfile> response = restTemplate.exchange(
//                KAKAO_USERINFO_URL,
//                HttpMethod.GET,
//                entity,
//                KakaoProfile.class
//        );
//
//        return response.getBody();
//    }
//
//    // 카카오 사용자 정보를 기반으로 회원을 생성하고 저장하는 메서드
//    public User processKakaoLogin(KakaoProfile kakaoProfile) {
//        String kakaoId = String.valueOf(kakaoProfile.getId());
//
//        // 카카오 ID로 기존 사용자가 있는지 확인
//        return userRepository.findByKakaoId(kakaoId)  // 인스턴스 메서드로 호출
//                .orElseGet(() -> {
//                    // 존재하지 않으면 새로운 회원 생성
//                    User newMember = new User();
//                    newMember.setKakaoId(kakaoId);
//                    newMember.setNickname(kakaoProfile.getKakaoAccount().getProfile().getNickname());
//                    newMember.setGender(kakaoProfile.getKakaoAccount().getGender());
//                    newMember.setProfilePicture(kakaoProfile.getKakaoAccount().getProfile().getProfileImageUrl());
//                    return userRepository.save(newMember);
//                });
//    }
//}
