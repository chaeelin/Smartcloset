package com.example.smartcloset.User.controller;

import com.example.smartcloset.User.dto.*;
import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.User.security.JwtUtil;
import com.example.smartcloset.User.service.KakaoService;
import com.example.smartcloset.User.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

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
                    user.getId(),
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

    // 일반로그인
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        try {
            User user = userService.getUserByLoginId(request.getLoginId());
            if (user != null && passwordEncoder.matches(request.getLoginPwd(), user.getLoginPwd())) {
                String token = jwtUtil.generateToken(user.getLoginId(), user.getId());  // 두 개의 매개변수 전달
                return new LoginResponse(token, request.getLoginId(), user.getNickname());
            } else {
                return new LoginResponse(null, request.getLoginId(), null);
            }
        } catch (Exception e) {
            return new LoginResponse(null, null, null);
        }
    }

    @PostMapping("/kakao/login")
    public ResponseEntity<KakaoResponse> kakaoLogin(@RequestBody KakaoUserDto kakaoUserDto) {
        Optional<User> existingUser = userService.findByKakaoId(kakaoUserDto.getKakaoId());

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            String token = jwtUtil.generateToken(user.getLoginId(), user.getId());  // 두 개의 매개변수 전달
            System.out.println("로그인");
            System.out.println("Generated JWT Token: " + token);

            KakaoResponse response = new KakaoResponse(
                    token,
                    user.getKakaoId(),
                    user.getNickname(),
                    user.getProfilePicture()
            );

            return ResponseEntity.ok(response);
        } else {
            User newUser = kakaoService.saveKakaoUser(kakaoUserDto);
            String token = jwtUtil.generateToken(newUser.getLoginId(), newUser.getId());  // 두 개의 매개변수 전달
            System.out.println("회원가입");
            System.out.println("Generated JWT Token: " + token);

            KakaoResponse response = new KakaoResponse(
                    token,
                    newUser.getKakaoId(),
                    newUser.getNickname(),
                    newUser.getProfilePicture()
            );

            return ResponseEntity.ok(response);
        }
    }

    // 아이디 중복
    @GetMapping("/check/loginId")
    public CheckResponse checkLoginId(@RequestParam String loginId) {
        if (userService.checkLoginId(loginId)) {
            return new CheckResponse(false); // loginId가 이미 존재하는 경우
        } else {
            return new CheckResponse(true);  // loginId가 존재하지 않는 경우
        }
    }

    // 닉네임 중복
    @GetMapping("/check/nickname")
    public CheckResponse checkNickname(@RequestParam String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            return new CheckResponse(false);
        } else {
            boolean exists = userService.checkNickname(nickname);
            return new CheckResponse(!exists);
        }
    }

    // 키와 몸무게 변경
    @PatchMapping("profile/update")
    public ResponseEntity<UpdateResponse> updateHeightAndWeight(@RequestBody UpdateRequest request) {
        try {
            Long userId = getAuthenticatedUserId(); // userId를 가져오는 메서드로 변경
            User user = userService.getUserById(userId);

            if (user != null) {
                userService.updateHeightAndWeight(user.getId(), request.getHeight(), request.getWeight());
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
            Long userId = getAuthenticatedUserId();
            User user = userService.getUserById(userId);

            if (user != null) {
                userService.deleteUser(user.getId());
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
            Long userId = getAuthenticatedUserId();
            User user = userService.getUserById(userId);

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
            Long userId = getAuthenticatedUserId();
            User user = userService.getUserById(userId);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found or invalid.");
            }

            userService.changeNickname(user, request.getNewNickname());
            return ResponseEntity.ok("Nickname changed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while changing the password.");
        }
    }

    // 프로필 사진 설정
    @PutMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = getAuthenticatedUserId();
            User user = userService.getUserById(userId);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String uniqueFilename = userId + "_" + UUID.randomUUID().toString() + extension;

            Path path = Paths.get("uploads/profile-pictures/" + uniqueFilename);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            user.setProfilePicture(path.toString());
            userService.updateUser(user);

            return ResponseEntity.ok("File uploaded successfully and profile picture updated");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/profile-picture")
    public ResponseEntity<byte[]> getProfilePicture(HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            Long userId = jwtUtil.extractUserId(token); // userId를 추출하는 메서드 사용

            User user = userService.getUserById(userId);

            if (user == null || user.getProfilePicture() == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Path path = Paths.get(user.getProfilePicture());
            byte[] data = Files.readAllBytes(path);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(data.length);

            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private Long getAuthenticatedUserId() {
        // SecurityContextHolder에서 인증된 사용자 ID를 추출하는 로직
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByLoginId(loginId);
        return user.getId();
    }
}
