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
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/user")
    public ResponseEntity<List<Post>> getPostsByUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(null); // 인증되지 않은 사용자
        }

        User user = userService.getUserByPrincipal(principal);
        if (user == null) {
            return ResponseEntity.status(401).build(); // 사용자 정보를 찾을 수 없는 경우
        }

        List<Post> userPosts = postService.getPostsByUser(user);
        if (userPosts.isEmpty()) {
            return ResponseEntity.notFound().build(); // 사용자가 작성한 게시글이 없는 경우
        }

        return ResponseEntity.ok(userPosts);
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
    public Post createPost(@RequestBody Post post, Principal principal) {
        User user = userService.getUserByPrincipal(principal);
        post.setUser(user);
        return postService.savePost(post);
    }

    @PostMapping("/withImage")
    public ResponseEntity<Post> createPostWithImage(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            Principal principal) {
        try {
            if (imageFile == null || imageFile.isEmpty()) {
                return ResponseEntity.badRequest().body(null); // 400 Bad Request 반환
            }

            User user = userService.getUserByPrincipal(principal);
            Post post = postService.savePostWithImage(title, content, imageFile, user);
            return ResponseEntity.ok(post);
        } catch (IOException e) {
            e.printStackTrace(); // 로그 출력
            return ResponseEntity.status(500).body(null);
        } catch (Exception e) {
            e.printStackTrace(); // 로그 출력
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post updatedPost, Principal principal) {
        try {
            User user = userService.getUserByPrincipal(principal);
            Post post = postService.getPostById(id);

            if (post.getUser().getId().equals(user.getId())) {
                Post updatedPostResult = postService.updatePost(id, updatedPost);
                return ResponseEntity.ok(updatedPostResult);
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
            Post post = postService.getPostById(id);

            if (post == null) {
                return ResponseEntity.notFound().build();  // 게시글을 찾을 수 없는 경우
            }

            if (!post.getUser().getId().equals(user.getId())) {
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
            @RequestParam(required = false) Long lastLikedId,
            @RequestParam(defaultValue = "10") int limit) {
        User user = userService.getUserByPrincipal(principal);
        List<Post> likedPosts = postService.getLikedPostsByUserWithScroll(user, lastLikedId, limit);
        return ResponseEntity.ok(likedPosts);
    }
}
