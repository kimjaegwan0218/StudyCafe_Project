package com.example.pentagon.repository;

import com.example.pentagon.domain.community.Post;
import com.example.pentagon.domain.enums.NoticeType;
import com.example.pentagon.domain.enums.PostStatus;
import com.example.pentagon.domain.enums.PostType;
import com.example.pentagon.repository.search.PostSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> , PostSearch {
    // 특정 사용자가 작성한 문의글만 가져오기 (1:1 문의의 핵심!)
    List<Post> findByUserEmailAndPostType(String email, String postType);

    @Query(value = "select now()", nativeQuery = true)
    String getTime();

    List<Post> findTop3ByOrderByLikedCountDesc();

    List<Post> findTop3ByPostTypeOrderByLikedCountDesc(PostType postType);

    List<Post> findByPostTypeOrderByCreatedAtDesc(PostType postType);

    List<Post> findByPostTypeAndNoticeTypeOrderByCreatedAtDesc(PostType postType, NoticeType noticeType);

    Optional<Post> findByIdAndPostType(Long id, PostType postType);

    List<Post> findByPostTypeAndStatusInOrderByCreatedAtDesc(PostType postType, List<PostStatus> statuses);

    List<Post> findByPostTypeAndNoticeTypeAndStatusInOrderByCreatedAtDesc(
            PostType postType, NoticeType noticeType, List<PostStatus> statuses
    );

    List<Post> findByPostTypeAndStatusOrderByCreatedAtDesc(PostType postType, PostStatus status);

    List<Post> findByPostTypeAndNoticeTypeAndStatusOrderByCreatedAtDesc(
            PostType postType, NoticeType noticeType, PostStatus status
    );
}