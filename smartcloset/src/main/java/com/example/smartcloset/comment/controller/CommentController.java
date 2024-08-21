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

    @PostMapping("/{feedId}")
    public ResponseEntity<?> saveComment(@PathVariable(name = "feedId") Long feedId,
                                         @RequestBody CommentRequestDto commentRequestDto,
                                         Principal principal) {
        Long commentId = commentService.save(feedId, commentRequestDto, principal.getName());
        return Response.onSuccess(commentId);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable(name = "commentId") Long commentId,
                                           Principal principal) {
        commentService.delete(commentId, principal.getName());
        return Response.onSuccess();
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable(name = "commentId") Long commentId,
                                           @RequestParam(name = "content") String content,
                                           Principal principal) {
        commentService.update(commentId, content, principal.getName());
        return Response.onSuccess();
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<?> reportComment(@PathVariable(name = "commentId") Long commentId) {
        commentService.report(commentId);
        return Response.onSuccess();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getComments(@PathVariable(name = "postId") Long postId){
        List<CommentResponseDto> commentResponseDtos = commentService.getAll(postId);
        return Response.onSuccess(commentResponseDtos);
    }
}
