package com.example.smartcloset.repository;

import com.example.smartcloset.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByTitleContainingIgnoreCase(String title);

    // 페이징 처리를 위한 메서드 추가
    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
