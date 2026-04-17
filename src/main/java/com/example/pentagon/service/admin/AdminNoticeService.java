package com.example.pentagon.service.admin;

import com.example.pentagon.dto.admin.AdminNoticeItemResponse;
import com.example.pentagon.dto.admin.AdminNoticeUpsertRequest;

import java.util.List;

public interface AdminNoticeService {
    List<AdminNoticeItemResponse> list(String cat);
    AdminNoticeItemResponse create(Long adminUserId, AdminNoticeUpsertRequest req);
    AdminNoticeItemResponse update(Long adminUserId, Long id, AdminNoticeUpsertRequest req);
    void delete(Long adminUserId, Long id);

}