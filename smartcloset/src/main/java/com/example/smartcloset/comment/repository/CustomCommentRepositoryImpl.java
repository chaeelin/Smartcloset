package com.example.smartcloset.comment.repository;

import com.example.smartcloset.comment.entity.CommentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CustomCommentRepositoryImpl implements CustomCommentRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void batchUpdate(List<CommentEntity> commentEntities,
                            Map<Long, Integer> commentIdAndReportCount) {
        String sql = "update comment set report_count=? where comment_id = ?";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CommentEntity comment = commentEntities.get(i);
                Long commentId = comment.getId();
                ps.setInt(1, comment.getReportCount()
                        + commentIdAndReportCount.get(commentId));
                ps.setLong(2, commentId);
            }

            @Override
            public int getBatchSize() {
                return commentEntities.size();
            }
        });
    }
}
