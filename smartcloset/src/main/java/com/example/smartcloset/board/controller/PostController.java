package com.example.smartcloset.board.controller;

import com.example.smartcloset.board.model.PostDTO;
import com.example.smartcloset.board.event.LikeEvent;
import com.example.smartcloset.board.service.PostService;
import com.example.smartcloset.comment.entity.CommentEntity;
import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.User.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    // 생성자 주입
    public PostController(PostService postService, UserService userService, ApplicationEventPublisher eventPublisher) {
        this.postService = postService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        List<PostDTO> postDtos = postService.getAllPosts();
        return ResponseEntity.ok(postDtos);
    }

    @GetMapping("/user")
    public ResponseEntity<List<PostDTO>> getPostsByUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(null); // 인증되지 않은 사용자
        }

        User user = userService.getUserByPrincipal(principal);
        if (user == null) {
            return ResponseEntity.status(401).build(); // 사용자 정보를 찾을 수 없는 경우
        }

        List<PostDTO> userPostDtos = postService.getPostsByUser(user);
        return ResponseEntity.ok(userPostDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        PostDTO postDto = postService.getPostById(id);
        if (postDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(postDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostDTO>> searchPosts(@RequestParam String title) {
        List<PostDTO> postDtos = postService.searchPostsByTitle(title);
        return ResponseEntity.ok(postDtos);
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody PostDTO postDto, Principal principal) {
        // loginId를 사용하여 User 객체 조회
        String loginId = postDto.getLoginId(); // userEmail 필드를 loginId로 사용합니다.

        if (loginId == null) {
            return ResponseEntity.badRequest().body(null); // loginId가 null인 경우
        }

        User user = userService.getUserById(loginId);
        if (user == null) {
            return ResponseEntity.badRequest().body(null); // 유효하지 않은 loginId인 경우
        }

        // PostDTO에 사용자 정보 설정
        postDto.setUserId(user.getId());
        postDto.setUserName(user.getNickname());

        PostDTO savedPostDto = postService.savePost(postDto);
        return ResponseEntity.ok(savedPostDto);
    }

    @PostMapping("/withImage")
    public ResponseEntity<PostDTO> createPostWithImage(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            Principal principal) {
        try {
            if (imageFile == null || imageFile.isEmpty()) {
                return ResponseEntity.badRequest().body(null); // 400 Bad Request 반환
            }

            User user = userService.getUserByPrincipal(principal);
            PostDTO postDto = postService.savePostWithImage(title, content, imageFile, user);
            return ResponseEntity.ok(postDto);
        } catch (IOException e) {
            e.printStackTrace(); // 로그 출력
            return ResponseEntity.status(500).body(null);
        } catch (Exception e) {
            e.printStackTrace(); // 로그 출력
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long id, @RequestBody PostDTO postDto, Principal principal) {
        try {
            User user = userService.getUserByPrincipal(principal);
            PostDTO existingPostDto = postService.getPostById(id);

            if (existingPostDto.getUserId().equals(user.getId())) {
                PostDTO updatedPostDto = postService.updatePost(id, postDto);
                return ResponseEntity.ok(updatedPostDto);
            } else {
                return ResponseEntity.status(403).build(); // Forbidden: 사용자가 게시글 작성자가 아닌 경우
            }
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<String> likePost(@PathVariable Long id, Principal principal) {
        User user = userService.getUserByPrincipal(principal);
        if (postService.hasUserLikedPost(user, id)) {
            return ResponseEntity.badRequest().body("User already liked this post.");
        }
        postService.likePost(user, id);
        eventPublisher.publishEvent(new LikeEvent(this, id));
        return ResponseEntity.ok("Post liked successfully.");
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<String> unlikePost(@PathVariable Long id, Principal principal) {
        User user = userService.getUserByPrincipal(principal);
        if (!postService.hasUserLikedPost(user, id)) {
            return ResponseEntity.badRequest().body("User has not liked this post.");
        }
        postService.unlikePost(user, id);
        return ResponseEntity.ok("Post unliked successfully.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Principal principal) {
        try {
            User user = userService.getUserByPrincipal(principal);
            PostDTO postDto = postService.getPostById(id);

            if (postDto == null) {
                return ResponseEntity.notFound().build();  // 게시글을 찾을 수 없는 경우
            }

            if (!postDto.getUserId().equals(user.getId())) {
                return ResponseEntity.status(403).build(); // 사용자가 게시글 작성자가 아닌 경우
            }

            postService.deletePost(id);
            return ResponseEntity.noContent().build();  // 성공적으로 삭제된 경우
        } catch (Exception e) {
            e.printStackTrace(); // 로그 출력
            return ResponseEntity.status(500).build();  // 서버 내부 오류 반환
        }
    }

    @GetMapping("/loadMore")
    public ResponseEntity<List<PostDTO>> loadMorePosts(@RequestParam Long lastPostId, @RequestParam int limit) {
        List<PostDTO> postDtos = postService.getPostsAfterId(lastPostId, limit);
        return ResponseEntity.ok(postDtos);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentEntity>> getCommentsByPostId(@PathVariable Long postId) {
        List<CommentEntity> comments = postService.getCommentsByPostId(postId);
        if (comments.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/liked")
    public ResponseEntity<List<PostDTO>> getLikedPosts(Principal principal) {
        User user = userService.getUserByPrincipal(principal);
        List<PostDTO> likedPostDtos = postService.getLikedPostsByUser(user);
        return ResponseEntity.ok(likedPostDtos);
    }

    @GetMapping("/liked/scroll")
    public ResponseEntity<List<PostDTO>> getLikedPostsWithScroll(
            Principal principal,
            @RequestParam(required = false) Long lastLikedId,
            @RequestParam(defaultValue = "10") int limit) {
        User user = userService.getUserByPrincipal(principal);
        List<PostDTO> likedPostDtos = postService.getLikedPostsByUserWithScroll(user, lastLikedId, limit);
        return ResponseEntity.ok(likedPostDtos);
    }
}
