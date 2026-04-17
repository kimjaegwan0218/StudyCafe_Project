package com.example.pentagon.service;

import com.example.pentagon.domain.User;
import com.example.pentagon.domain.community.Comment;
import com.example.pentagon.domain.community.Post;
import com.example.pentagon.domain.community.PostLike;
import com.example.pentagon.domain.community.PostLikeId;
import com.example.pentagon.domain.enums.CommentStatus;
import com.example.pentagon.domain.enums.PostStatus;
import com.example.pentagon.domain.enums.PostType;
import com.example.pentagon.dto.community.CommentDTO;
import com.example.pentagon.dto.community.PageRequestDTO;
import com.example.pentagon.dto.community.PageResponseDTO;
import com.example.pentagon.dto.community.PostDTO;
import com.example.pentagon.repository.CommentRepository;
import com.example.pentagon.repository.PostLikeRepository;
import com.example.pentagon.repository.PostRepository;
import com.example.pentagon.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    @Override
    public Long register(PostDTO postDTO, Long userId) {

        User user = userRepository.findById(userId).orElseThrow();

        Post post = Post.builder()
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .user(user) // 🔥 로그인 유저
                .postType(PostType.COMMUNITY)
                .status(PostStatus.NORMAL)
                .likedCount(0)
                .build();

        return postRepository.save(post).getId();
    }


    @Override
    public PostDTO readOne(Long id) {
        Post post = postRepository.findById(id).orElseThrow();
        return entityToDTO(post);
    }


    @Transactional
    @Override
    public void modify(PostDTO postDTO, Long loginUserId) {

        Post post = postRepository.findById(postDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        // 🔐 작성자 체크
        if (!post.getUser().getId().equals(loginUserId)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        post.change(postDTO.getTitle(), postDTO.getContent());
    }

    @Transactional
    @Override
    public void remove(Long postId, Long loginUserId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        // 🔐 작성자 체크
        if (!post.getUser().getId().equals(loginUserId)) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }

    @Override
    public PageResponseDTO<PostDTO> list(PageRequestDTO pageRequestDTO) {

        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("id");

        Page<Post> result = postRepository.searchAll(PostType.COMMUNITY,types, keyword, pageable);

        List<PostDTO> dtoList = result.getContent().stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<PostDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();

    }

    @Override
    public List<PostDTO> getTop3Posts() {
        // Repository에서 이미 만든 findTop3ByOrderByLikedCountDesc 호출
        List<Post> result = postRepository.findTop3ByPostTypeOrderByLikedCountDesc(PostType.COMMUNITY);

        return result.stream()
                .map(this::entityToDTO) // 엔티티를 DTO로 변환
                .collect(Collectors.toList());
    }

    @Override
    public void toggleLike(Long postId, Long userId) {
        // 1. 유저와 게시글 조회
        Post post = postRepository.findById(postId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        // 2. 복합키 생성
        PostLikeId likeId = new PostLikeId(postId, userId);

        // 3. 좋아요 존재 여부 확인
        Optional<PostLike> postLike = postLikeRepository.findById(likeId);

        if (postLike.isPresent()) {
            postLikeRepository.delete(postLike.get());
            post.updateLikedCount(false); // 엔티티의 비즈니스 메서드 사용 권장
            log.info("하트 취소됨! 현재 하트 수: " + post.getLikedCount());
        } else {
            PostLike newLike = PostLike.builder()
                    .id(likeId)
                    .post(post)
                    .user(user)
                    .build();
            postLikeRepository.save(newLike);
            post.updateLikedCount(true); // 엔티티의 비즈니스 메서드 사용 권장
            log.info("하트 클릭됨! 현재 하트 수: " + post.getLikedCount());
        }
    }

    @Override
    public List<CommentDTO> getComment(Long postId) {
        // 1. Repository를 통해 특정 게시글의 댓글 리스트를 가져옵니다.
        // CommentRepository에 findByPostId 메서드가 있어야 합니다.
        List<Comment> result = commentRepository.findByPostId(postId);

        // 2. 엔티티 리스트를 DTO 리스트로 변환하여 반환합니다.
        return result.stream().map(comment -> CommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .userId(comment.getUser().getId())
                .text(comment.getContent()) // 엔티티의 content를 DTO의 text로
                .userName(comment.getUser().getName()) // 작성자 이름
                .createdAt(comment.getCreatedAt())
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public Long registerComment(CommentDTO dto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        Comment comment = Comment.builder()
                .post(post)
                .user(user)               // 🔥 이 줄이 핵심
                .content(dto.getText())
                .status(CommentStatus.NORMAL)
                .build();

        return commentRepository.save(comment).getId();
    }

    @Transactional
    public void removeComment(Long commentId, Long userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow();

        if (!comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("댓글 작성자만 삭제 가능");
        }

        commentRepository.delete(comment);
    }


    @Override
    @Transactional
    public void removeWithComments(Long id, Long userId) {
        log.info("게시글 및 관련 데이터 삭제 시작: " + id);

        postLikeRepository.deleteByPostId(id);

        commentRepository.deleteByPostId(id);

        postRepository.deleteById(id);

    }

    @Override
    public PostDTO read(Long id) { //게시글 조회

        Optional<Post> result = postRepository.findById(id);
        Post post = result.orElseThrow();

        return modelMapper.map(post, PostDTO.class);
    }

}







