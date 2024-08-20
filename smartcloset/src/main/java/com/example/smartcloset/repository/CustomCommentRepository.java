package com.example.smartcloset.repository;

import com.example.smartcloset.entity.CommentEntity;

import java.util.List;
import java.util.Map;

public interface CustomCommentRepository {
    void batchUpdate(List<CommentEntity> commentEntities,
                     Map<Long, Integer> commentIdAndReportCount);
}