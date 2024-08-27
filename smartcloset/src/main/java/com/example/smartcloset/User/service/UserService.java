package com.example.smartcloset.User.service;

import com.example.smartcloset.User.entity.Platform;
import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.User.entity.Gender;
import com.example.smartcloset.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.Principal;
import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ID를 통해 사용자 조회
    public User getUserById(String loginId) {
        System.out.println("Fetching member by loginId: " + loginId);
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid login_id: " + loginId));
    }

    // 현재 로그인된 사용자 정보를 가져오기
    public User getUserByPrincipal(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Principal cannot be null");
        }

        String loginId = principal.getName();

        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with loginId: " + loginId));
    }


    public boolean checkLoginId(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    public boolean checkNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    // 회원가입
    public User register(String loginId, String loginPwd, String nickname, int height, int weight, Platform platform, Gender gender) {
        // Platform이 null일 경우 기본값으로 APP 설정
        if (platform == null) {
            platform = Platform.APP;
        }

        User user = User.builder()
                .loginId(loginId)
                .loginPwd(loginPwd)
                .nickname(nickname)
                .height(height)
                .weight(weight)
                .platform(platform)
                .date(new Timestamp(System.currentTimeMillis()))
                .gender(gender)
                .build();
        return saveUser(user);
    }

    // 키 몸무게 수정
    public void updateHeightAndWeight(Long userId, Integer height, Integer weight) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (height != null) {
            user.setHeight(height);
        }
        if (weight != null) {
            user.setWeight(weight);
        }
        saveUser(user);
    }

    // 회원 삭제
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // 비번 변경
    public void changePassword(User user, String newPassword) {
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.setLoginPwd(encryptedPassword);
        saveUser(user);
    }

    // 닉네임 변경
    public void changeNickname(User user, String newNickname) {
        user.setNickname(newNickname);
        saveUser(user);
    }

    // 유저 저장
    private User saveUser(User user) {
        return userRepository.save(user);
    }

    // 유저 update
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
