package com.example.smartcloset.comment.controller;

import com.example.smartcloset.comment.dto.CommentRequestDto;
import com.example.smartcloset.comment.dto.CommentResponseDto;
import com.example.smartcloset.comment.service.CommentService;
import com.example.smartcloset.global.common.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    /**
     * 특정 게시글에 댓글 추가
     */
    @PostMapping("/{postId}")
    public ResponseEntity<?> saveComment(@PathVariable(name = "postId") Long postId,
                                         @RequestBody CommentRequestDto commentRequestDto,
                                         Principal principal) {
        Long commentId = commentService.save(postId, commentRequestDto, principal.getName());
        return Response.onSuccess(commentId);
    }

    /**
     * 특정 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable(name = "commentId") Long commentId,
                                           Principal principal) {
        commentService.delete(commentId, principal.getName());
        return Response.onSuccess();
    }

    /**
     * 특정 댓글의 내용 수정
     */
    @PatchMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable(name = "commentId") Long commentId,
                                           @RequestParam(name = "content") String content,
                                           Principal principal) {
        commentService.update(commentId, content, principal.getName());
        return Response.onSuccess();
    }

    /**
     * 특정 댓글 신고
     */
    @PatchMapping("/{commentId}/report")
    public ResponseEntity<?> reportComment(@PathVariable(name = "commentId") Long commentId) {
        commentService.report(commentId);
        return Response.onSuccess();
    }

    /**
     * 특정 게시물의 전체 댓글 조회 -> 성능상 문제가 있을 시 페이지네이션 추가
     */
    @GetMapping("/{postId}")
    public ResponseEntity<?> getComments(@PathVariable(name = "postId") Long postId) {
        List<CommentResponseDto> commentResponseDtos = commentService.getAll(postId);
        return Response.onSuccess(commentResponseDtos);
    }
}
