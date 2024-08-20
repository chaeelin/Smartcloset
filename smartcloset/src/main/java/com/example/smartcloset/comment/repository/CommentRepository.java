package com.example.smartcloset.comment.repository;

import com.example.smartcloset.comment.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long>, CustomCommentRepository {
    @Query("select c from CommentEntity c join fetch c.user where c.id=?1")
    Optional<CommentEntity> findByIdWithUser(Long commentId);

    @Query("select c.id from CommentEntity c where c.parent_id=?1 limit 1")
    boolean existsByParentId(Long commentId);
}
