package com.example.pentagon.dto.inquiry;


import com.example.pentagon.domain.enums.InquiryStatus;
import com.example.pentagon.domain.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminInquiryDetailDTO {

    // inquiry
    private Long inquiryId;

    private Long writerId;
    private String writerName;
    private String writerEmail;
    private String writerPhone;
    private UserRole writerRole;

    private String title;
    private String content;
    private InquiryStatus status;

    private LocalDateTime inquiryCreatedAt;
    private LocalDateTime inquiryUpdatedAt;

    // reply (없으면 null)
    private Long replyId;
    private String replyContent;
    private LocalDateTime replyCreatedAt;
}
