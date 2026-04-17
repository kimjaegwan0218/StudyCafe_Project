package com.example.pentagon.service.admin;


import com.example.pentagon.domain.support.InquiryReply;

import java.util.Optional;

public interface AdminInquiryReplyService {

    Long insertReply(Long inquiryId, Long userId, String content);

    void updateReply(Long inquiryId, String content);

    void deleteReply(Long inquiryId);
}
