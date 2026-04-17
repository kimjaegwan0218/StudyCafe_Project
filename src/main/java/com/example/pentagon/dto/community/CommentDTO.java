package com.example.pentagon.dto.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {

    private Long id;
    private Long postId;    // 어떤 게시글의 댓글인지
    private Long userId;
    private String text;    // 댓글 내용
    private String userName; // 작성자 이름
    private LocalDateTime createdAt;
}
