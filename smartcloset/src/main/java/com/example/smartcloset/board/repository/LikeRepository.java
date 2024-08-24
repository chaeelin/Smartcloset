package com.example.smartcloset.board.repository;

import com.example.smartcloset.board.model.LikeEntity;
import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.board.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {

    // 특정 사용자가 특정 게시물에 좋아요를 눌렀는지 확인
    Optional<LikeEntity> findByUserAndPost(User user, Post post);

    // 사용자가 좋아요한 모든 게시물을 최신순으로 조회 (페이징 지원)
    List<LikeEntity> findByUserOrderByCreatedDateDesc(User user, Pageable pageable);

    // 특정 ID 이후에 사용자가 좋아요한 게시물들을 최신순으로 조회
    List<LikeEntity> findByUserAndIdLessThanOrderByCreatedDateDesc(User user, Long lastLikedId, Pageable pageable);

    // 사용자가 좋아요한 모든 게시물 조회
    List<LikeEntity> findByUser(User user);

    // 특정 게시물의 모든 좋아요 삭제
    void deleteByPost(Post post);
}