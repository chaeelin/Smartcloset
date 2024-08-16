package com.example.smartcloset.service;

import com.example.smartcloset.model.Post;
import com.example.smartcloset.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public List<Post> searchPostsByTitle(String title) {
        return postRepository.findByTitleContainingIgnoreCase(title);
    }

    // 페이징을 고려한 게시물 검색
    public Page<Post> searchPostsByTitleWithPaging(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    public Post savePost(Post post) {
        return postRepository.save(post);


    }

    // 게시물 업데이트 메서드
    public Post updatePost(Long id, Post updatedPost) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        existingPost.setImageUrl(updatedPost.getImageUrl());
        // 필요한 경우, 다른 필드들도 업데이트

        return postRepository.save(existingPost);
    }

    public void increasePostLikes(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);
    }

    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found");
        }
        postRepository.deleteById(id);
    }

    // Additional business logic (e.g., update, delete)
}
