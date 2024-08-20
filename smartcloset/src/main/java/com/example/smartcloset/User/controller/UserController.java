package com.example.smartcloset.User.controller;

import com.example.smartcloset.User.dto.*;
import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.User.repository.UserRepository;
import com.example.smartcloset.User.security.JwtUtil;
import com.example.smartcloset.User.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest request) {
        try {
            // 비밀번호를 BCrypt로 암호화
            String encodedPassword = passwordEncoder.encode(request.getLoginPwd());

            // 사용자 등록
            User user = userService.register(
                    request.getLoginId(),
                    encodedPassword, // 암호화된 비밀번호를 사용
                    request.getNickname(),
                    request.getHeight(),
                    request.getWeight(),
                    request.getPlatform(),
                    request.getGender()
            );

            // 등록된 사용자 정보를 UserResponse로 변환
            UserResponse userResponse = new UserResponse(
                    user.getUser_id(),
                    user.getLoginId(),
                    user.getNickname(),
                    user.getHeight(),
                    user.getWeight(),
                    user.getGender()
            );

            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            // 에러가 발생한 경우
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // 에러 시 null 또는 에러 메시지 반환
        }
    }


    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        try {
            System.out.println("Login request received for ID: " + request.getLoginId());
            User user = userService.getUserById(request.getLoginId());

            if (user != null) {
                System.out.println("Member found : " + request.getLoginId());

                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

                // 입력된 비밀번호를 암호화된 비밀번호와 비교
                if (passwordEncoder.matches(request.getLoginPwd(), user.getLoginPwd())) {
                    String token = jwtUtil.generateToken(user.getLoginId());
                    return new LoginResponse(token, request.getLoginId(), user.getNickname());
                } else {
                    System.out.println("Invalid login_pwd for ID: " + request.getLoginId());
                    return new LoginResponse(null, request.getLoginId(), null);
                }
            } else {
                System.out.println("Invalid login_id: " + request.getLoginId());
                return new LoginResponse(null, null, null);
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            return new LoginResponse(null, null, null);
        }
    }

    // 아이디 중복 확인 API
    @GetMapping("/check/loginId")
    public CheckResponse checkLoginId(@RequestParam String loginId) {
        if (userService.checkLoginId(loginId)) {
            // loginId가 이미 존재하는 경우
            System.out.println("Already Exists " + loginId);
            return new CheckResponse(false);
        } else {
            // loginId가 존재하지 않는 경우
            System.out.println("loginId is available " + loginId);
            return new CheckResponse(true);
        }
    }

    // 닉네임 중복
    @GetMapping("/check/nickname")
    public CheckResponse checkNickname(@RequestParam("nickname") String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            System.out.println("nickname is empty.");
            return new CheckResponse(false);
        } else {
            boolean exists = userService.checkNickname(nickname);
            if (exists) {
                System.out.println("Already Exists " + nickname);
                return new CheckResponse(false);
            } else {
                System.out.println("Nickname is available " + nickname);
                return new CheckResponse(true);
            }
        }
    }

    // 키, 몸무게 수정
    @PatchMapping("profile/update")
    public ResponseEntity<UpdateResponse> updateHeightAndWeight(@RequestBody UpdateRequest request) {
        try {
            // SecurityContextHolder에서 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loginId = authentication.getName();

            User user = userService.getUserById(loginId);

            if (user != null) {
                userService.updateHeightAndWeight(user.getUser_id(), request.getHeight(), request.getWeight());
                UpdateResponse response = new UpdateResponse(
                        request.getHeight(),
                        request.getWeight()
                );
                return ResponseEntity.ok(response);
            } else {
                // 사용자가 존재하지 않는 경우
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            // 예외 발생 시 내부 서버 오류로 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    // 회원탈퇴
    @DeleteMapping("profile/delete")
    public String deleteUser() {
        try {
            // SecurityContextHolder에서 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loginId = authentication.getName();

            User user = userService.getUserById(loginId);

            if (user != null) {
                userService.deleteUser(user.getUser_id());
                return "User deleted successfully.";
            } else {
                return "User not found.";
            }
        } catch (Exception e) {
            return "An error occurred while deleting the user.";
        }
    }

    // 비밀번호 변경
    @PatchMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest request) {
        try {
            // 현재 로그인한 사용자의 ID를 SecurityContext에서 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loginId = authentication.getName();

            // 사용자 조회
            User user = userService.getUserById(loginId);

            // 사용자가 존재하는지 확인할 필요가 없습니다.
            // 이미 getUserById 메서드에서 찾지 못하면 예외를 던지므로 별도 체크는 필요 없습니다.

            // 현재 비밀번호 확인
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getLoginPwd())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect.");
            }

            // 비밀번호 변경
            user.setLoginPwd(passwordEncoder.encode(request.getNewPassword()));

            // 변경된 사용자 정보를 저장
            userService.updateUser(user);

            return ResponseEntity.ok("Password changed successfully.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while changing the password.");
        }
    }




    // 닉네임 변경
    @PatchMapping("/change-nickname")
    public ResponseEntity<CheckResponse> changeNickname(@RequestBody NicknameChangeRequest request) {
        try {
            // SecurityContextHolder에서 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loginId = authentication.getName();

            // 사용자 조회
            User user = userService.getUserById(loginId);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CheckResponse(false));
            }

            // 닉네임 변경
            userService.changeNickname(user.getLoginId(), request.getNewNickname());
            return ResponseEntity.ok(new CheckResponse(true));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CheckResponse(false));
        }
    }


    // 프로필 사진 업데이트 API
    @PutMapping("/profile-picture")
    public String updateProfilePicture(@RequestParam("file") MultipartFile file) throws IOException {
        userService.updateProfilePicture(file);
        return "Profile picture updated successfully";
    }

    // 프로필 사진 가져오기 API
    @GetMapping("/profile-picture")
    public byte[] getProfilePicture() throws IOException {
        String picturePath = userService.getProfilePicturePath();
        if (picturePath != null) {
            return Files.readAllBytes(Paths.get(picturePath));
        } else {
            return null; // 프론트엔드에서 기본 이미지를 처리하도록 null 반환
        }
    }
}

