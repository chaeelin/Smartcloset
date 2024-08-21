package com.example.smartcloset.User.controller;

import com.example.smartcloset.User.dto.*;
import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.User.security.JwtUtil;
import com.example.smartcloset.User.service.KakaoService;
import com.example.smartcloset.User.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final KakaoService kakaoService;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, KakaoService kakaoService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.kakaoService = kakaoService;
    }

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest request) {
        try {
            String encodedPassword = passwordEncoder.encode(request.getLoginPwd());

            User user = userService.register(
                    request.getLoginId(),
                    encodedPassword,
                    request.getNickname(),
                    request.getHeight(),
                    request.getWeight(),
                    request.getPlatform(),
                    request.getGender()
            );

            UserResponse userResponse = new UserResponse(
                    user.getUser_id(),
                    user.getLoginId(),
                    user.getNickname(),
                    user.getHeight(),
                    user.getWeight(),
                    user.getGender(),
                    user.getPlatform()
            );

            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 일반 로그인
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        try {
            User user = userService.getUserById(request.getLoginId());
            if (user != null && passwordEncoder.matches(request.getLoginPwd(), user.getLoginPwd())) {
                String token = jwtUtil.generateToken(user.getLoginId());
                return new LoginResponse(token, request.getLoginId(), user.getNickname());
            } else {
                return new LoginResponse(null, request.getLoginId(), null);
            }
        } catch (Exception e) {
            return new LoginResponse(null, null, null);
        }
    }


    // 네이버 소셜 로그인
    @GetMapping("/naver/login")
    public ResponseEntity<LoginResponse> naverLogin(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        User user = userService.processNaverUser(oAuth2User);
        String token = jwtUtil.generateToken(user.getLoginId());

        return ResponseEntity.ok(new LoginResponse(token, user.getLoginId(), user.getNickname()));
    }

    // 카카오 소셜 로그인
    @PostMapping("/kakao/login")
    public ResponseEntity<LoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        try {
            KakaoProfile kakaoProfile = kakaoService.getKakaoProfile(request.getAccessToken());
            User user = kakaoService.processKakaoLogin(kakaoProfile);

            if (user != null) {
                String token = jwtUtil.generateToken(user.getLoginId());
                LoginResponse response = new LoginResponse("Login successful", user.getNickname(), token);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(401).body(new LoginResponse("Login failed for Kakao ID", null, null));
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(new LoginResponse("Error during Kakao login: " + e.getMessage(), null, null));
        }
    }

    // 아이디 중복
    @GetMapping("/check/loginId")
    public CheckResponse checkLoginId(@RequestParam String loginId) {
        if (userService.checkLoginId(loginId)) {
            // loginId가 이미 존재하는 경우
            return new CheckResponse(false);
        } else {
            // loginId가 존재하지 않는 경우
            return new CheckResponse(true);
        }
    }

    // 닉네임 중복
    @GetMapping("/check/nickname")
    public CheckResponse checkNickname(@RequestParam String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            return new CheckResponse(false);
        } else {
            boolean exists = userService.checkNickname(nickname);
            if (exists) {
                return new CheckResponse(false);
            } else {
                return new CheckResponse(true);
            }
        }
    }

    // 키와 몸무게 변경
    @PatchMapping("profile/update")
    public ResponseEntity<UpdateResponse> updateHeightAndWeight(@RequestBody UpdateRequest request) {
        try {
            String loginId = getAuthenticatedUserName();
            User user = userService.getUserById(loginId);

            if (user != null) {
                userService.updateHeightAndWeight(user.getUser_id(), request.getHeight(), request.getWeight());
                UpdateResponse response = new UpdateResponse(request.getHeight(), request.getWeight());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 회원탈퇴
    @DeleteMapping("profile/delete")
    public String deleteUser() {
        try {
            String loginId = getAuthenticatedUserName();
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
    @PatchMapping("/change/password")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest request) {
        try {
            String loginId = getAuthenticatedUserName();
            User user = userService.getUserById(loginId);

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getLoginPwd())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect.");
            }

            userService.changePassword(user, request.getNewPassword());
            return ResponseEntity.ok("Password changed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while changing the password.");
        }
    }

    // 닉네임 변경
    @PatchMapping("/change/nickname")
    public ResponseEntity<String> changeNickname(@RequestBody NicknameChangeRequest request) {
        try {
            String loginId = getAuthenticatedUserName();
            User user = userService.getUserById(loginId);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found or invalid.");
            }

            userService.changeNickname(user, request.getNewNickname());
            return ResponseEntity.ok("Nickname changed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while changing the password.");
        }
    }

    // 프로필 변경
    @PutMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        try {
            String loginId = getAuthenticatedUserName();
            User user = userService.getUserById(loginId);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            // 파일 저장 경로 설정
            String filename = file.getOriginalFilename();
            Path path = Paths.get("uploads/profile-pictures/" + filename);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            // 사용자 엔티티에 프로필 사진 경로 저장
            user.setProfilePicture(path.toString()); // 전체 파일 경로 저장
            userService.updateUser(user);

            return ResponseEntity.ok("File uploaded successfully and profile picture updated");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }

    // 키, 몸무게 변경
    @GetMapping("/profile-picture")
    public ResponseEntity<byte[]> getProfilePicture() {

        String loginId = getAuthenticatedUserName();
        User user = userService.getUserById(loginId);

        if (user == null || user.getProfilePicture() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            Path path = Paths.get(user.getProfilePicture());
            byte[] data = Files.readAllBytes(path);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(data.length);

            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private String getAuthenticatedUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}

