package com.example.smartcloset.board.service;

import com.example.smartcloset.board.model.Post;
import com.example.smartcloset.board.model.PostDTO;
import com.example.smartcloset.board.event.LikeEvent;
import com.example.smartcloset.board.event.TopPostEvent;
import com.example.smartcloset.board.model.LikeEntity;
import com.example.smartcloset.board.repository.PostRepository;
import com.example.smartcloset.board.repository.LikeRepository;
import com.example.smartcloset.comment.entity.CommentEntity;
import com.example.smartcloset.comment.repository.CommentRepository;
import com.example.smartcloset.User.entity.User;
import com.example.smartcloset.User.service.UserService;
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
    private final UserService userService;  // UserService 추가
    private final ApplicationEventPublisher eventPublisher;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public PostService(PostRepository postRepository, CommentRepository commentRepository,
                       LikeRepository likeRepository, UserService userService, ApplicationEventPublisher eventPublisher) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.userService = userService;  // UserService 주입
        this.eventPublisher = eventPublisher;
    }

    public List<PostDTO> getAllPosts() {
        List<Post> posts = postRepository.findAllWithUser();
        return posts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        return convertToDto(post);
    }

    public List<PostDTO> getPostsByUser(User user) {
        List<Post> posts = postRepository.findByUser(user);
        return posts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<PostDTO> searchPostsByTitle(String title) {
        List<Post> posts = postRepository.findByTitleContainingIgnoreCase(title);
        return posts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public PostDTO savePost(PostDTO postDto) {
        Post post = convertToEntity(postDto);
        Post savedPost = postRepository.save(post);
        return convertToDto(savedPost);
    }

    public PostDTO updatePost(Long id, PostDTO postDto) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        existingPost.setTitle(postDto.getTitle());
        existingPost.setContent(postDto.getContent());
        existingPost.setImageUrl(postDto.getImageUrl());

        Post updatedPost = postRepository.save(existingPost);
        return convertToDto(updatedPost);
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
        commentRepository.deleteRepliesByPostId(id); // 대댓글 먼저 삭제
        commentRepository.deleteCommentsByPostId(id); // 부모 댓글 삭제
        likeRepository.deleteByPost(postRepository.findById(id).get());
        postRepository.deleteById(id);
        return true;  // 성공적으로 삭제된 경우 true 반환
    }

    public List<PostDTO> getPostsAfterId(Long lastPostId, int limit) {
        List<Post> posts = postRepository.findByIdGreaterThanOrderByIdAsc(lastPostId, PageRequest.of(0, limit));
        return posts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional
    public PostDTO savePostWithImage(String title, String content, MultipartFile imageFile, User user) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }

        String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(imageFile.getOriginalFilename());
        Path path = Paths.get(uploadDir + fileName);

        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new IOException("Could not create directories to save file.", e);
        }

        try {
            Files.write(path, imageFile.getBytes());
        } catch (IOException e) {
            throw new IOException("Failed to save image file.", e);
        }

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setUser(user);
        post.setImageUrl("/uploads/" + fileName);
        post.setDate(LocalDateTime.now());

        Post savedPost = postRepository.save(post);
        return convertToDto(savedPost);
    }

    public List<PostDTO> getLikedPostsByUser(User user) {
        List<LikeEntity> likedEntities = likeRepository.findByUser(user);
        return likedEntities.stream()
                .map(LikeEntity::getPost)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PostDTO> getLikedPostsByUserWithScroll(User user, Long lastLikedId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdDate"));

        List<Post> likedPosts;
        if (lastLikedId != null) {
            likedPosts = likeRepository.findByUserAndIdLessThanOrderByCreatedDateDesc(user, lastLikedId, pageable)
                    .stream()
                    .map(LikeEntity::getPost)
                    .collect(Collectors.toList());
        } else {
            likedPosts = likeRepository.findByUserOrderByCreatedDateDesc(user, pageable)
                    .stream()
                    .map(LikeEntity::getPost)
                    .collect(Collectors.toList());
        }

        return likedPosts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public PostDTO convertToDto(Post post) {
        if (post == null) {
            System.out.println("Post is null.");
            return null; // null 체크 추가
        }

        User user = post.getUser(); // 게시물 작성자 정보 가져오기

        if (user == null) {
            System.out.println("User is null for Post ID: " + post.getId());
        } else {
            System.out.println("User ID: " + user.getId() + ", User Name: " + user.getNickname());
        }

        return new PostDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getLikes(),
                post.getDate(),
                post.getImageUrl(),
                post.getCommentsCount(),
                user != null ? user.getId() : null, // 사용자 null 체크
                user != null ? user.getNickname() : null, // user.getNickname() 사용
                user != null ? user.getLoginId() : null  // user.getLoginId() 사용
        );
    }

    public Post convertToEntity(PostDTO postDto) {
        User user = null;
        if (postDto.getUserId() != null) {
            // userId가 존재할 경우 UserService를 통해 User 객체를 조회합니다.
            user = userService.getUserById(postDto.getUserId());
            if (user == null) {
                throw new IllegalArgumentException("Invalid user ID: " + postDto.getUserId());
            }
        }

        Post post = new Post();
        post.setId(postDto.getId());
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setLikes(postDto.getLikes());
        post.setDate(postDto.getDate());
        post.setImageUrl(postDto.getImageUrl());
        post.setCommentsCount(postDto.getCommentsCount());
        post.setUser(user);

        return post;
    }
}
