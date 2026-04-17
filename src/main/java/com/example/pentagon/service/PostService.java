package com.example.pentagon.service;

import com.example.pentagon.domain.community.Post;
import com.example.pentagon.dto.community.CommentDTO;
import com.example.pentagon.dto.community.PageRequestDTO;
import com.example.pentagon.dto.community.PageResponseDTO;
import com.example.pentagon.dto.community.PostDTO;

import java.util.List;

public interface PostService {

    Long register(PostDTO postDTO, Long userId);

    PostDTO readOne(Long id);

    void modify(PostDTO postDTO, Long userId);

    void remove(Long id, Long userId);

    void removeWithComments(Long id, Long userId);

    // -----> comment 등록 및 삭제 부분

    List<CommentDTO> getComment(Long postId);

    void removeComment(Long commentId, Long userId);

    Long registerComment(CommentDTO commentDTO, Long userId);

    PageResponseDTO<PostDTO> list(PageRequestDTO pageRequestDTO);

    //사용자 게시물 수정
    PostDTO read(Long id);

    // 좋아요 토글
    void toggleLike(Long postId, Long userId);

    // 메인 상단 인기 게시글 3개 가져오기
    List<PostDTO> getTop3Posts();


    default PostDTO entityToDTO(Post post) {
        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .likedCount(post.getLikedCount())
                .userName(post.getUser().getName()) // 엔티티의 User 객체에서 이름만 추출
                .userId(post.getUser().getId())     // 수정/삭제 권한 확인용 ID
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }




}

