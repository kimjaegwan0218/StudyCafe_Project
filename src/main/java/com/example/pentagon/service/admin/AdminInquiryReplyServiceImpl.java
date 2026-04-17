package com.example.pentagon.service.admin;


import com.example.pentagon.domain.User;
import com.example.pentagon.domain.support.Inquiry;
import com.example.pentagon.domain.support.InquiryReply;
import com.example.pentagon.repository.InquiryReplyRepository;
import com.example.pentagon.repository.InquiryRepository;
import com.example.pentagon.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
@Transactional
public class AdminInquiryReplyServiceImpl implements AdminInquiryReplyService {

    private final InquiryReplyRepository inquiryReplyRepository;

    private final InquiryRepository inquiryRepository;

    private final UserRepository userRepository;

    @Override
    public Long insertReply(Long inquiryId, Long userId, String content) {

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("content is empty");
        }

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalStateException("inquiry not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("user not found"));

        // 핵심: 문의 1개당 reply 1개 전제 -> 기존 reply 있으면 삭제
        inquiryReplyRepository.deleteByInquiry_Id(inquiryId);
        inquiryReplyRepository.flush();

        InquiryReply reply = InquiryReply.builder()
                .inquiry(inquiry)
                .user(user)
                .content(content)
                .build();

        InquiryReply saved = inquiryReplyRepository.save(reply);
        return saved.getId();
    }

    @Override
    public void updateReply(Long inquiryId, String content) {

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("content is empty");
        }

        InquiryReply reply = inquiryReplyRepository.findByInquiry_Id(inquiryId)
                .orElseThrow(() -> new IllegalStateException("reply not found"));

        reply.setContent(content);
        inquiryReplyRepository.save(reply);
    }

    @Override
    public void deleteReply(Long inquiryId) {
        inquiryReplyRepository.deleteByInquiry_Id(inquiryId);
        inquiryReplyRepository.flush();
    }

}
