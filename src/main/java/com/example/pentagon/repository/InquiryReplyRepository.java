package com.example.pentagon.repository;


import com.example.pentagon.domain.support.InquiryReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryReplyRepository extends JpaRepository<InquiryReply, Long> {

    // 문의 1개당 reply 1개 전제면 이게 핵심
    Optional<InquiryReply> findByInquiry_Id(Long inquiryId);

    void deleteByInquiry_Id(Long inquiryId);

    Optional<InquiryReply> findByInquiry_IdAndUser_Id(Long inquiryId, Long userId);

    boolean existsByInquiryId(Long inquiryId);


}



