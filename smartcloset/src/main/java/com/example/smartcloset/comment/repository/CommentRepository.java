package com.example.smartcloset.comment.repository;

import com.example.smartcloset.comment.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CommentRepository
        extends JpaRepository<CommentEntity, Long>, CustomCommentRepository {

    boolean existsByParentId(Long commentId);
}