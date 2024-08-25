package com.example.smartcloset.board.service;

import com.example.smartcloset.board.event.LikeEvent;
import com.example.smartcloset.board.event.TopPostEvent;
import com.example.smartcloset.board.model.Post;
import com.example.smartcloset.board.model.LikeEntity;
import com.example.smartcloset.board.repository.PostRepository;
import com.example.smartcloset.board.repository.LikeRepository;
import com.example.smartcloset.comment.entity.CommentEntity;
import com.example.smartcloset.comment.repository.CommentRepository;
import com.example.smartcloset.User.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public PostService(PostRepository postRepository, CommentRepository commentRepository,
                       LikeRepository likeRepository, ApplicationEventPublisher eventPublisher) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
    }


    public List<Post> getPostsByUser(User user) {
        // 특정 사용자가 작성한 게시글 목록을 반환하는 로직을 구현합니다.
        return postRepository.findByUser(user);
    }

    public List<Post> searchPostsByTitle(String title) {
        return postRepository.findByTitleContainingIgnoreCase(title);
    }

    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    public Post updatePost(Long id, Post updatedPost) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        existingPost.setImageUrl(updatedPost.getImageUrl());

        return postRepository.save(existingPost);
    }

    public List<CommentEntity> getCommentsByPostId(Long postId) {
        return commentRepository.findAllByPostId(postId);
    }

    public void incrementCommentCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);
    }

    public void decrementCommentCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        post.setCommentsCount(post.getCommentsCount() - 1);
        postRepository.save(post);
    }

    public boolean hasUserLikedPost(User user, Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return false;
        return likeRepository.findByUserAndPost(user, post).isPresent();
    }

    @Transactional
    public void likePost(User user, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        LikeEntity like = new LikeEntity();
        like.setUser(user);
        like.setPost(post);
        likeRepository.save(like);

        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);
        eventPublisher.publishEvent(new LikeEvent(this, post.getId()));

        if (post.getLikes() >= 5) {
            eventPublisher.publishEvent(new TopPostEvent(this, post.getId()));
        }
    }

    @Transactional
    public void unlikePost(User user, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        LikeEntity like = likeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new RuntimeException("Like not found"));
        likeRepository.delete(like);

        post.setLikes(post.getLikes() - 1);
        postRepository.save(post);
    }

    @Transactional
    public boolean deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            return false;  // 게시물이 존재하지 않는 경우 false 반환
        }
        // 외래 키 제약 조건으로 인해 먼저 대댓글 및 부모 댓글을 삭제
        commentRepository.deleteRepliesByPostId(id); // 대댓글 먼저 삭제
        commentRepository.deleteCommentsByPostId(id); // 부모 댓글 삭제
        likeRepository.deleteByPost(postRepository.findById(id).get());
        postRepository.deleteById(id);
        return true;  // 성공적으로 삭제된 경우 true 반환
    }

    public List<Post> getPostsAfterId(Long lastPostId, int limit) {
        return postRepository.findByIdGreaterThanOrderByIdAsc(lastPostId, PageRequest.of(0, limit));
    }

    public Post savePostWithImage(String title, String content, MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }

        // 클린 파일 이름 생성
        String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(imageFile.getOriginalFilename());

        // 파일 저장 경로 설정
        Path path = Paths.get(uploadDir + fileName);

        // 디렉토리 생성 (존재하지 않는 경우)
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new IOException("Could not create directories to save file.", e);
        }

        // 파일을 서버에 저장
        try {
            Files.write(path, imageFile.getBytes());
        } catch (IOException e) {
            throw new IOException("Failed to save image file.", e);
        }

        // Post 객체 생성 및 데이터베이스에 저장
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);

        // 저장된 파일의 URL을 설정
        String fileDownloadUri = "/uploads/" + fileName; // 파일에 접근할 수 있는 URL 경로를 사용
        post.setImageUrl(fileDownloadUri);

        post.setDate(LocalDateTime.now());

        try {
            return postRepository.save(post);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save post to database.", e);
        }
    }

    public List<Post> getLikedPostsByUser(User user) {
        return likeRepository.findByUser(user).stream()
                .map(LikeEntity::getPost)
                .collect(Collectors.toList());
    }

    public List<Post> getLikedPostsByUserWithScroll(User user, Long lastLikedId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdDate"));

        // `lastLikedId`가 있는 경우 이후의 좋아요들을 조회합니다.
        if (lastLikedId != null) {
            return likeRepository.findByUserAndIdLessThanOrderByCreatedDateDesc(user, lastLikedId, pageable)
                    .stream()
                    .map(LikeEntity::getPost)
                    .collect(Collectors.toList());
        } else {
            // 첫 페이지 로드
            return likeRepository.findByUserOrderByCreatedDateDesc(user, pageable)
                    .stream()
                    .map(LikeEntity::getPost)
                    .collect(Collectors.toList());
        }
    }
}
