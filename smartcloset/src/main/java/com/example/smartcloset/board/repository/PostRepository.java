package com.example.smartcloset.board.repository;

import com.example.smartcloset.board.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.smartcloset.User.entity.User;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 제목에 특정 문자열이 포함된 게시물을 대소문자 구분 없이 검색
    List<Post> findByTitleContainingIgnoreCase(String title);

    // 특정 ID 이후의 게시물들을 가져오는 메서드 추가 (무한 스크롤 지원)
    List<Post> findByIdGreaterThanOrderByIdAsc(Long lastPostId, Pageable pageable);

    // 특정 사용자가 작성한 게시물을 가져오는 메서드 추가
    List<Post> findByUser(User user);
}