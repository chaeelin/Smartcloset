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

    // 로그인 ID를 통해 사용자를 조회
    public User getUserById(String loginId) {
        System.out.println("Fetching member by loginId: " + loginId);
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid login_id: " + loginId));
    }

    // Principal 객체를 사용하여 현재 로그인된 사용자 정보를 가져오는 메서드 추가
    public User getUserByPrincipal(Principal principal) {
        String loginId = principal.getName(); // Principal에서 사용자 이름(로그인 ID)을 가져옴
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

    // 네이버
    public User processNaverUser(OAuth2User oAuth2User) {
        String loginId = (String) oAuth2User.getAttributes().get("id"); // 유니크한 네이버 ID 사용
        String nickname = (String) oAuth2User.getAttributes().get("nickname");
        String profileImage = (String) oAuth2User.getAttributes().get("profile_image");
        String gender = (String) oAuth2User.getAttributes().get("gender");

        // 사용자가 이미 존재하는지 확인
        User user = userRepository.findByLoginId(loginId)
                .orElseGet(() -> {
                    // 존재하지 않으면 새로운 사용자 등록
                    User newUser = User.builder()
                            .loginId(loginId)
                            .loginPwd(null)
                            .nickname(nickname)
                            .height(0)
                            .weight(0)
                            .platform(Platform.NAVER)
                            .date(new Timestamp(System.currentTimeMillis()))
                            .gender(Gender.valueOf(gender.toUpperCase()))
                            .profilePicture(profileImage)
                            .build();
                    return userRepository.save(newUser);
                });

        user.setNickname(nickname);
        user.setProfilePicture(profileImage);
        userRepository.save(user);

        return user;
    }

    // 키, 몸무게 수정
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

    // 회원 탈퇴
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // 비밀번호 변경
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

    private User saveUser(User user) {
        return userRepository.save(user);
    }

    // 사용자 업데이트 메서드
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
