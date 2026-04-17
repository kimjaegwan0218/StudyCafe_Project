package com.example.pentagon.service.admin;


import com.example.pentagon.domain.enums.InquiryStatus;
import com.example.pentagon.dto.inquiry.AdminInquiryDetailDTO;
import com.example.pentagon.dto.inquiry.AdminInquiryListDTO;
import org.springframework.data.domain.Page;

public interface AdminInquiryService {

    Page<AdminInquiryListDTO> getList(InquiryStatus status, int page, int size);

    Page<AdminInquiryListDTO> getListByUser(Long userId, InquiryStatus status, int page, int size);

    Page<AdminInquiryListDTO> getAll(int page, int size);

    Page<AdminInquiryListDTO> getByUser(Long userId, int page, int size);

    AdminInquiryDetailDTO getDetail(Long inquiryId);

}
