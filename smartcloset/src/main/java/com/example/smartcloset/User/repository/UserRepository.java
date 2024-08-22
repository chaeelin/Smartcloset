package com.example.smartcloset.User.repository;

import com.example.smartcloset.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
    boolean existsByNickname(String nickname);
    // 카카오 ID로 회원을 찾는 메서드 추가
    Optional<User> findByKakaoId(String kakaoId);  // 카카오 ID로 사용자 조회
}