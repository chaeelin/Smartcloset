package com.example.smartcloset.User.service;

import com.example.smartcloset.User.dto.KakaoUserDto;
import com.example.smartcloset.User.entity.Platform;
import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.User.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class KakaoService {

    private final UserRepository userRepository;

    @Autowired
    public KakaoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveKakaoUser(KakaoUserDto kakaoUserDto) {
        // 카카오 ID로 기존 사용자를 찾거나 새로운 사용자 생성
        User user = userRepository.findByKakaoId(kakaoUserDto.getKakaoId())
                .orElseGet(() -> createUserFromKakaoUserDto(kakaoUserDto));

        return user;
    }

    // KakaoUserDto 기반으로 새로운 사용자 생성
    private User createUserFromKakaoUserDto(KakaoUserDto kakaoUserDto) {
        User newUser = User.builder()
                .kakaoId(kakaoUserDto.getKakaoId())
                .loginId(kakaoUserDto.getKakaoId())
                .nickname(kakaoUserDto.getNickname())
                .profilePicture(kakaoUserDto.getProfilePicture())
                .platform(Platform.KAKAO)
                .build();

        return userRepository.save(newUser);
    }
}
