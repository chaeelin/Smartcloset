package com.example.smartcloset.comment.service;

import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.User.repository.UserRepository;
import com.example.smartcloset.board.model.Post;
import com.example.smartcloset.board.repository.PostRepository;
import com.example.smartcloset.comment.dto.CommentRequestDto;
import com.example.smartcloset.comment.dto.CommentResponseDto;
import com.example.smartcloset.comment.entity.CommentEntity;
import com.example.smartcloset.comment.repository.CommentRepository;
import com.example.smartcloset.global.common.service.RedisService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RedisService redisService;

    @Transactional
    public Long save(Long postId, CommentRequestDto commentRequestDto, String userName) {
        CommentEntity parent = null;
        Long parentId = commentRequestDto.getParentId();
        if (parentId != null) {
            parent = commentRepository.findById(parentId).orElseThrow(/*예외 발생*/);
        }
        Post post = postRepository.findById(postId).orElseThrow(/*예외 발생*/);
        User user = userRepository.findByLoginId(userName).orElseThrow(/*예외 발생*/);
        CommentEntity comment = CommentEntity.builder()
                .parent(parent).user(user).post(post)
                .reportCount(0).content(commentRequestDto.getContent())
                .build();
        commentRepository.save((comment));
        return comment.getId();
    }

    /**
     * 삭제시 대댓글이 존재하면 삭제된 댓글로 변경
     * 대댓글이 없으면 아예 삭제
     */
    @Transactional
    public void delete(Long commentId, String userName) {
        CommentEntity comment = commentRepository.findByIdWithUser(commentId)
                .orElseThrow(/* 예외 발생*/);
        if (comment.getUser().getLoginId().equals(userName)) {
            if (commentRepository.existsByParentId(commentId)) {
                comment.setContent("삭제된 댓글입니다.");
                commentRepository.save(comment);
            } else {
                commentRepository.delete(comment);
            }
        } else {
            /*예외 발생*/
        }
    }

    @Transactional
    public void update(Long commentId, String content, String userName) {
        CommentEntity comment = commentRepository.findByIdWithUser(commentId)
                .orElseThrow(/* 예외 발생*/);
        if (comment.getUser().getLoginId().equals(userName)) {
            comment.setContent(content);
            commentRepository.save(comment);
        } else {
            /*예외 발생*/
        }
    }

    /**
     * 댓글의 신고 수를 redis에 우선 저장
     */
    @Transactional
    public void report(Long commentId) {
        redisService.addReportCount(commentId);
    }

    /**
     * fixedDelay에 설정된 시간마다 redis에 저장된
     * 댓글의 신고 수를 업데이트
     */
    @Scheduled(fixedDelay = 3000000, initialDelay = 3000000)
    public void reportCountToDB() {
        Set<String> allKeys = redisService.getAllKeys();
        List<Long> onlyKeys = allKeys.stream()
                .map(key -> Long.parseLong(key.split(":")[1])).toList();
        Map<Long, Integer> commentIdAndReportCount = redisService.getReportCount(onlyKeys);
        int keySize = onlyKeys.size();
        for (int i = 0; i < keySize; ) {
            List<CommentEntity> commentEntities = commentRepository.findAllById(
                    onlyKeys.subList(i, i += i + 100 < keySize ? 100 : keySize % 100));
            commentRepository.batchUpdate(commentEntities, commentIdAndReportCount);
        }
    }

    public List<CommentResponseDto> getComments(Long postId, Long lastCommentId) {
        return commentRepository.findCommentsByPostId(postId, lastCommentId);
    }
}