package com.example.smartcloset.User.service;

import com.example.smartcloset.User.dto.KakaoProfile;
import com.example.smartcloset.User.entity.Gender;
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
        headers.setBearerAuth(accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            System.out.println("Requesting Kakao profile with access token: " + accessToken);
            ResponseEntity<KakaoProfile> response = restTemplate.exchange(
                    KAKAO_USERINFO_URL,
                    HttpMethod.GET,
                    entity,
                    KakaoProfile.class
            );
            System.out.println("Kakao profile response: " + response.getBody());
            return response.getBody();
        } catch (RestClientException e) {
            System.out.println("Error occurred while getting Kakao profile: " + e.getMessage());
            throw new RuntimeException("Failed to get Kakao profile", e);
        }
    }

    public User processKakaoLogin(KakaoProfile kakaoProfile) {
        String kakaoId = String.valueOf(kakaoProfile.getId());
        System.out.println("Processing Kakao login for user with Kakao ID: " + kakaoId);

        // 기존 사용자 검색 -> 없으면 추가
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> createUserFromKakaoProfile(kakaoProfile)); // 추가됨: 사용자가 없을 경우 새로 생성

        return user;
    }

    // 카카오 프로필 기반 새로운 생성자 만들기
    private User createUserFromKakaoProfile(KakaoProfile kakaoProfile) {

//        // genderString을 사용하여 Gender 타입으로 변환합니다
//        String genderString = null;
//        Gender gender = convertStringToGender(genderString);  // 문자열을 Gender로 변환합니다

        User newUser = User.builder()
                .kakaoId(String.valueOf(kakaoProfile.getId()))
                .loginId(String.valueOf(kakaoProfile.getId()))
                .nickname(kakaoProfile.getKakao_account().getProfile().getNickname())
//                .gender(gender)
                .profilePicture(kakaoProfile.getKakao_account().getProfile().getProfile_image())
                .platform(Platform.KAKAO)  // 예시로 플랫폼을 카카오로 지정
                .build();

        return userRepository.save(newUser);
    }

/*    private Gender convertStringToGender(String genderString) {
        if (genderString == null) {
            return Gender.MALE;  // genderString이 null인 경우 UNKNOWN을 반환
        }
        switch (genderString.toUpperCase()) {
            case "MALE":
                return Gender.MALE;
            case "FEMALE":
                return Gender.FEMALE;
            default:
                return Gender.MALE;  // 인식할 수 없는 값인 경우 UNKNOWN을 반환
        }
    }*/
}