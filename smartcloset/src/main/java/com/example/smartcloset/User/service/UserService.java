package com.example.smartcloset.User.service;

import com.example.smartcloset.User.entity.Platform;
import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.User.entity.Gender;
import com.example.smartcloset.User.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Optional;

@Service
public class UserService {

    @Value("${profile.pictures.dir}")
    private String profilePicturesDir;
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 로그인 ID를 통해 사용자를 조회
    public User getUserById(String loginId) {
        System.out.println("Fetching member by loginId: " + loginId);
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid login_id: " + loginId));
    }

    public boolean checkLoginId(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    public boolean checkNickname(String nickname) {

        return userRepository.existsByNickname(nickname);
    }

    // 회원가입
    public User register(String loginId, String loginPwd, String nickname, int height, int weight, Platform platform, Gender gender) {
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
        userRepository.save(user);
        return user; // 여기서 User 객체를 반환해야 합니다.
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
        userRepository.save(user);
    }


    // 회원 탈퇴
    public void deleteUser(Long userId) {

        userRepository.deleteById(userId);
    }

    // 로그인 ID로 사용자 조회
    public Optional<User> getUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId);
    }

    // 사용자 업데이트 메소드 추가
    public void updateUser(User user) {
        userRepository.save(user); // save 메소드는 존재하는 사용자를 업데이트
    }

    // 비번 변경
    public void changePassword(String loginId, String newPassword) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with loginId: " + loginId));

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(newPassword);

        user.setLoginPwd(encryptedPassword);
        userRepository.save(user);
    }


    // 닉네임 변경
    public void changeNickname(String loginId, String newNickname) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with loginId: " + loginId));

        user.setNickname(newNickname);
        userRepository.save(user);
    }

    // 프로필 사진 경로 가져오기
    public String getProfilePicturePath() {
        User user = getAuthenticatedUser(); // 현재 인증된 사용자를 가져옵니다.
        if (user.getProfilePicture() == null) {
            return null;
        } else {
            return Paths.get(profilePicturesDir, user.getProfilePicture()).toString();
        }
    }

    // 프로필 사진 업데이트
    public void updateProfilePicture(MultipartFile file) throws IOException {
        User user = getAuthenticatedUser(); // 현재 인증된 사용자를 가져옵니다.
        if (!file.isEmpty()) {
            String filename = user.getUser_id() + "_" + file.getOriginalFilename();
            Path destinationPath = Paths.get(profilePicturesDir, filename);
            file.transferTo(destinationPath.toFile());

            user.setProfilePicture(filename);
            saveUser(user);
        }
    }

    private void saveUser(User user) {
        userRepository.save(user);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
