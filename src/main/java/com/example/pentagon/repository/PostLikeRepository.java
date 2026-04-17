package com.example.pentagon.repository;

import com.example.pentagon.domain.community.PostLike;
import com.example.pentagon.domain.community.PostLikeId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    @Modifying // 데이터를 변경하는 쿼리임을 나타냄 (삭제/수정 시 필수)
    @Transactional // 삭제 작업 도중 에러 발생 시 롤백을 보장함
    @Query("delete from PostLike pl where pl.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
