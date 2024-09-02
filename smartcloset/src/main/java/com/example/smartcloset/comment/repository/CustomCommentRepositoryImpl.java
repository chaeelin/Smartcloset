package com.example.smartcloset.comment.repository;

import com.example.smartcloset.comment.dto.CommentResponseDto;
import com.example.smartcloset.comment.dto.QCommentResponseDto;
import com.example.smartcloset.comment.entity.CommentEntity;
import com.example.smartcloset.comment.entity.QCommentEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.smartcloset.User.entity.QUser.user;
import static com.example.smartcloset.comment.entity.QCommentEntity.commentEntity;


@RequiredArgsConstructor
public class CustomCommentRepositoryImpl implements CustomCommentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JPAQueryFactory jpaQueryFactory;

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

    public void deleteCommentsByPostId(Long postId) {
        jpaQueryFactory
                .delete(commentEntity)
                .where(commentEntity.post.id.eq(postId)
                        .and(commentEntity.parent.id.isNull()))
                .execute();
    }

    @Override
    public Optional<CommentEntity> findByIdWithUser(Long commentId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(commentEntity)
                        .join(commentEntity.user)
                        .fetchJoin()
                        .where(commentEntity.id.eq(commentId))
                        .fetchOne());
    }

    @Override
    public List<CommentResponseDto> findCommentsByPostId(Long postId, Long lastCommentId) {
        QCommentEntity parent = new QCommentEntity("parent");
        return jpaQueryFactory
                .select(new QCommentResponseDto(
                        commentEntity.id,
                        commentEntity.content,
                        commentEntity.reportCount,
                        parent.id,
                        user.nickname
                ))
                .from(commentEntity)
                .join(commentEntity.user, user)
                .leftJoin(commentEntity.parent, parent)
                .where(commentIdGoe(lastCommentId))
                .orderBy(commentEntity.id.asc())
                .limit(3)
                .fetch();

    }

    public void deleteRepliesByPostId(Long postId) {
        jpaQueryFactory
                .delete(commentEntity)
                .where(commentEntity.post.id.eq(postId)
                        .and(commentEntity.parent.id.isNotNull()))
                .execute();
    }

    private BooleanExpression commentIdGoe(Long commentId){
        return commentId!=null ? commentEntity.id.gt(commentId) : null;
    }
}
