package com.example.smartcloset.comment.repository;

import com.example.smartcloset.comment.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long>, CustomCommentRepository {
    @Query("select c from CommentEntity c join fetch c.user where c.id=?1")
    Optional<CommentEntity> findByIdWithUser(Long commentId);

    boolean existsByParentId(Long commentId);

    @Modifying
    @Query("delete from CommentEntity c where c.post.id=?1")
    void deleteByPostId(Long postId);

    @Query("select c from CommentEntity c join fetch c.user " +
            "left join fetch c.parent where c.post.id=?1 order by c.id")
    List<CommentEntity> findAllByPostId(Long postId);
}