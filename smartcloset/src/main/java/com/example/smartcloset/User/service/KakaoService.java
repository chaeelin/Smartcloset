package com.example.smartcloset.User.service;

import com.example.smartcloset.User.dto.KakaoProfile;
import com.example.smartcloset.User.dto.KakaoProperties;
import com.example.smartcloset.User.entity.Platform;
import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.User.repository.UserRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

import java.util.Map;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KakaoService {

    private static final String KAKAO_USERINFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    private final KakaoProperties kakaoProperties;
    private final UserRepository userRepository;

    @Autowired
    public KakaoService(KakaoProperties kakaoProperties, UserRepository userRepository) {
        this.kakaoProperties = kakaoProperties;
        this.userRepository = userRepository;
    }

    private final WebClient webClient = WebClient.builder()
            .baseUrl(KAKAO_TOKEN_URL)
            .build();

    public String getAccessToken(String authorizationCode) {
        try {
            Mono<Map<String, Object>> responseMono = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("grant_type", "authorization_code")
                            .queryParam("client_id", kakaoProperties.getClientId())
                            .queryParam("redirect_uri", kakaoProperties.getRedirectUri())
                            .queryParam("code", authorizationCode)
                            .build())
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});

            Map<String, Object> responseBody = responseMono.block();  // Use block() carefully
            if (responseBody != null && responseBody.containsKey("access_token")) {
                return (String) responseBody.get("access_token");
            } else {
                throw new RuntimeException("Failed to retrieve access token from Kakao");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get access token", e);
        }
    }

    public KakaoProfile getKakaoProfile(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("property_keys", "%5B%22kakao_account.profile%22%5D");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

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

    public User processKakaoLogin(String authorizationCode) {
        // 인가 코드를 사용해 액세스 토큰을 얻음
        String accessToken = getAccessToken(authorizationCode);
        // 액세스 토큰을 사용해 카카오 프로필 정보 조회
        KakaoProfile kakaoProfile = getKakaoProfile(accessToken);
        // 카카오 ID로 기존 사용자를 찾거나 새로운 사용자 생성
        String kakaoId = String.valueOf(kakaoProfile.getId());

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> createUserFromKakaoProfile(kakaoProfile));

        return user;
    }

    // 카카오 프로필 기반 새로운 생성자 만들기
    private User createUserFromKakaoProfile(KakaoProfile kakaoProfile) {

        User newUser = User.builder()
                .kakaoId(String.valueOf(kakaoProfile.getId()))
                .loginId(String.valueOf(kakaoProfile.getId()))
                .nickname(kakaoProfile.getKakao_account().getProfile().getNickname())
                .profilePicture(kakaoProfile.getKakao_account().getProfile().getProfile_image())
                .platform(Platform.KAKAO)
                .build();

        return userRepository.save(newUser);
    }
}