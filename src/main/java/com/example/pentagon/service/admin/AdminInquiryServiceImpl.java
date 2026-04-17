package com.example.pentagon.service.admin;


import com.example.pentagon.domain.enums.InquiryStatus;
import com.example.pentagon.dto.inquiry.AdminInquiryDetailDTO;
import com.example.pentagon.dto.inquiry.AdminInquiryListDTO;
import com.example.pentagon.repository.InquiryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInquiryServiceImpl implements AdminInquiryService {

    private final InquiryRepository inquiryRepository;

    private PageRequest pageRequest(int page, int size) {
        // 프론트는 보통 1페이지부터, PageRequest는 0부터 시작
        int p = Math.max(page - 1, 0);
        int s = Math.max(size, 1);
        return PageRequest.of(p, s);
    }

    @Override
    public Page<AdminInquiryListDTO> getList(InquiryStatus status, int page, int size) {
        return inquiryRepository.findAdminInquiryList(status, pageRequest(page, size));
    }

    @Override
    public Page<AdminInquiryListDTO> getListByUser(Long userId, InquiryStatus status, int page, int size) {
        return inquiryRepository.findAdminInquiryListByUser(userId, status, pageRequest(page, size));
    }

    @Override
    public Page<AdminInquiryListDTO> getAll(int page, int size) {
        return inquiryRepository.findAdminInquiryAllList(pageRequest(page, size));
    }

    @Override
    public Page<AdminInquiryListDTO> getByUser(Long userId, int page, int size) {
        return inquiryRepository.findAdminInquiryAllListByUserId(userId, pageRequest(page, size));
    }

    @Override
    public AdminInquiryDetailDTO getDetail(Long inquiryId) {
        return inquiryRepository.findAdminInquiryDetail(inquiryId)
                .orElseThrow(() -> new EntityNotFoundException("Inquiry not found: " + inquiryId));
    }

}
