package com.example.smartcloset.board.controller;

import com.example.smartcloset.board.model.Post;
import com.example.smartcloset.board.service.PostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 생성자 주입
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    @GetMapping("/search")
    public List<Post> searchPosts(@RequestParam String title) {
        return postService.searchPostsByTitle(title);
    }

    // 페이징 메서드 제거

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        return postService.savePost(post);
    }

    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Long id, @RequestBody Post updatedPost) {
        return postService.updatePost(id, updatedPost);
    }

    @PutMapping("/{id}/like")
    public void likePost(@PathVariable Long id) {
        postService.increasePostLikes(id);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        postService.deletePost(id);
    }

    // 새로운 메서드 추가: 마지막으로 로드된 게시물 이후의 게시물들을 불러오는 메서드
    @GetMapping("/loadMore")
    public List<Post> loadMorePosts(@RequestParam Long lastPostId, @RequestParam int limit) {
        return postService.getPostsAfterId(lastPostId, limit);
    }
}