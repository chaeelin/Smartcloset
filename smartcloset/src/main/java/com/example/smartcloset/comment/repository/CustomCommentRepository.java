package com.example.smartcloset.comment.repository;

import com.example.smartcloset.comment.dto.CommentResponseDto;
import com.example.smartcloset.comment.entity.CommentEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CustomCommentRepository {
    /**
     * 댓글의 신고 개수 업데이트를 배치로 처리
     */
    void batchUpdate(List<CommentEntity> commentEntities,
                     Map<Long, Integer> commentIdAndReportCount);

    void deleteRepliesByPostId(Long postId);

    void deleteCommentsByPostId(Long postId);

    Optional<CommentEntity> findByIdWithUser(Long commentId);

    List<CommentResponseDto> findAllByPostId(Long postId);
}