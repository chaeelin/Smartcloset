package com.example.smartcloset.comment.repository;

import com.example.smartcloset.comment.entity.CommentEntity;

import java.util.List;
import java.util.Map;

public interface CustomCommentRepository {
    void batchUpdate(List<CommentEntity> commentEntities,
                     Map<Long, Integer> commentIdAndReportCount);
}