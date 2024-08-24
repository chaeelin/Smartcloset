package com.example.smartcloset.board.controller;

import com.example.smartcloset.board.event.LikeEvent;
import com.example.smartcloset.board.model.Post;
import com.example.smartcloset.board.service.PostService;
import com.example.smartcloset.comment.entity.CommentEntity;
import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.User.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(post);
    }

    @GetMapping("/search")
    public List<Post> searchPosts(@RequestParam String title) {
        return postService.searchPostsByTitle(title);
    }

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        return postService.savePost(post);
    }

    @PostMapping("/withImage")
    public ResponseEntity<Post> createPostWithImage(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart("imageFile") MultipartFile imageFile) {
        try {
            Post post = postService.savePostWithImage(title, content, imageFile);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post updatedPost) {
        try {
            Post post = postService.updatePost(id, updatedPost);
            return ResponseEntity.ok(post);
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
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (!postService.deletePost(id)) {
            return ResponseEntity.notFound().build();  // 삭제 실패한 경우 404 Not Found 반환
        }
        return ResponseEntity.noContent().build();  // 성공적으로 삭제된 경우 204 No Content 반환
    }

    @GetMapping("/loadMore")
    public List<Post> loadMorePosts(@RequestParam Long lastPostId, @RequestParam int limit) {
        return postService.getPostsAfterId(lastPostId, limit);
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
    public ResponseEntity<List<Post>> getLikedPosts(Principal principal) {
        User user = userService.getUserByPrincipal(principal);
        List<Post> likedPosts = postService.getLikedPostsByUser(user);
        return ResponseEntity.ok(likedPosts);
    }

    @GetMapping("/liked/scroll")
    public ResponseEntity<List<Post>> getLikedPostsWithScroll(
            Principal principal,
            @RequestParam(required = false) Long lastLikedId, // 마지막으로 로드된 좋아요 ID
            @RequestParam(defaultValue = "10") int limit) { // 가져올 좋아요 개수
        User user = userService.getUserByPrincipal(principal);
        List<Post> likedPosts = postService.getLikedPostsByUserWithScroll(user, lastLikedId, limit);
        return ResponseEntity.ok(likedPosts);
    }
}
