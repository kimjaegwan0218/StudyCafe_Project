package com.example.pentagon.dto.inquiry;


import com.example.pentagon.domain.enums.InquiryStatus;
import com.example.pentagon.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminInquiryListDTO {

    private Long replyId;        // ✅ null이면 답변 없음
    private Long inquiryId;

    private Long writerId;
    private String writerName;
    private UserRole writerRole;

    private String title;
    private String content;

    private InquiryStatus status;
    private LocalDateTime createdAt;
}
