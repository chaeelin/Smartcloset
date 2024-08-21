package com.example.smartcloset.board.service;

import com.example.smartcloset.board.event.LikeEvent;
import com.example.smartcloset.board.event.TopPostEvent;
import com.example.smartcloset.board.model.Post;
import com.example.smartcloset.board.repository.PostRepository;
import com.example.smartcloset.comment.repository.CommentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CommentRepository commentRepository;

    // 생성자를 통한 의존성 주입
    public PostService(PostRepository postRepository, ApplicationEventPublisher eventPublisher, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.eventPublisher = eventPublisher;
        this.commentRepository = commentRepository;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public List<Post> searchPostsByTitle(String title) {
        return postRepository.findByTitleContainingIgnoreCase(title);
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

    // 좋아요 수 증가 및 이벤트 발생
    public void increasePostLikes(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);

        // 좋아요 이벤트 발생
        eventPublisher.publishEvent(new LikeEvent(this, post.getId()));

        // 특정 수 이상의 좋아요일 경우 상단 노출 이벤트 발생
        if (post.getLikes() >= 5) { // 5는 예시 값입니다. 적절한 수치를 설정하세요.
            eventPublisher.publishEvent(new TopPostEvent(this, post.getId()));
        }
    }

    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found");
        }
        //외래키 먼저 삭제 후 게시글 삭제 진행
        commentRepository.deleteByPostId(id);
        postRepository.deleteById(id);
    }

    // 무한 스크롤을 위한 메서드 추가
    public List<Post> getPostsAfterId(Long lastPostId, int limit) {
        return postRepository.findByIdGreaterThanOrderByIdAsc(lastPostId, PageRequest.of(0, limit));
    }

    // 추가적인 비즈니스 로직 (예: 업데이트, 삭제 등)
}