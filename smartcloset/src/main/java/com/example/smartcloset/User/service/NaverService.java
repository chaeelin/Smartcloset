//package com.example.smartcloset.User.service;
//
//import com.example.smartcloset.User.entity.Gender;
//import com.example.smartcloset.User.entity.Platform;
//import com.example.smartcloset.User.entity.User;
//import com.example.smartcloset.User.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//
//@Service
//public class NaverService {
//
//    private final UserRepository userRepository;
//
//    @Autowired
//    public NaverService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    public User processNaverUser(OAuth2User oAuth2User) {
//        String loginId = (String) oAuth2User.getAttributes().get("id"); // 유니크한 네이버 ID 사용
//        String nickname = (String) oAuth2User.getAttributes().get("nickname");
//        String profileImage = (String) oAuth2User.getAttributes().get("profile_image");
//        String gender = (String) oAuth2User.getAttributes().get("gender");
//
//        User user = userRepository.findByLoginId(loginId)
//                .orElseGet(() -> {
//                    User newUser = User.builder()
//                            .loginId(loginId)
//                            .loginPwd(null)
//                            .nickname(nickname)
//                            .platform(Platform.NAVER)
//                            .gender(Gender.valueOf(gender.toUpperCase()))
//                            .profilePicture(profileImage)
//                            .build();
//                    return userRepository.save(newUser);
//                });
//
//        user.setNickname(nickname);
//        user.setProfilePicture(profileImage);
//        userRepository.save(user);
//
//        return user;
//    }
//}
