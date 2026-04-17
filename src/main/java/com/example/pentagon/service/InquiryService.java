package com.example.pentagon.service;

import com.example.pentagon.domain.User;
import com.example.pentagon.domain.enums.InquiryStatus;
import com.example.pentagon.domain.support.Inquiry;
import com.example.pentagon.repository.InquiryRepository;
import com.example.pentagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    // 1. [기존 유지] 마이페이지용 (내 문의 내역 페이징)
    public Page<Inquiry> getMyInquiries(String email, Pageable pageable) {
        return inquiryRepository.findAllByUser_EmailOrderByCreatedAtDesc(email, pageable);
    }

    // 2. [기존 유지] 전체 문의 목록 (관리자용 등)
    public Page<Inquiry> boardList(Pageable pageable) {
        return inquiryRepository.findAll(pageable);
    }

    // 3. [유지/확인] 문의글 상세 조회
    public Inquiry getInquiry(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의글이 없습니다."));
    }

    // 4. [유지/확인] 글 작성
    @Transactional
    public void write(User user, String title, String content) {
        Inquiry inquiry = new Inquiry();
        inquiry.setUser(user);
        inquiry.setTitle(title);
        inquiry.setContent(content);
        inquiry.setCreatedAt(LocalDateTime.now());
        inquiry.setStatus(InquiryStatus.WAITING);

        inquiryRepository.save(inquiry);
    }

    // 5. [수정됨] 글 수정 (InquiryForm 대신 String title, content 사용)
    @Transactional
    public void update(Long id, User user, String title, String content) {
        Inquiry inquiry = getInquiry(id);

        // 작성자 본인 확인 (ID로 비교)
        if (!inquiry.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        // 내용 변경 (더티 체킹으로 자동 저장됨)
        inquiry.setTitle(title);
        inquiry.setContent(content);
    }

    // 6. [수정됨] 글 삭제 (String username 대신 User 객체 사용)
    @Transactional
    public void delete(Long id, User user) {
        Inquiry inquiry = getInquiry(id);

        // 작성자 본인 확인
        if (!inquiry.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        inquiryRepository.delete(inquiry);
    }


}