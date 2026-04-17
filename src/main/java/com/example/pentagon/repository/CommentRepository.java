package com.example.pentagon.repository;

import com.example.pentagon.domain.community.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostId(Long postId);

    void deleteByPostId(Long postId);

//    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);
}
