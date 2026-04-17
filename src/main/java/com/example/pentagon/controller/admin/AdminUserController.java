package com.example.pentagon.controller.admin;


import com.example.pentagon.domain.enums.InquiryStatus;
import com.example.pentagon.dto.inquiry.AdminInquiryDetailDTO;
import com.example.pentagon.dto.inquiry.AdminInquiryListDTO;
import com.example.pentagon.dto.user.AdminUserDetailDTO;
import com.example.pentagon.dto.user.AdminUserListDTO;
import com.example.pentagon.service.admin.AdminInquiryService;
import com.example.pentagon.service.admin.AdminSubscriptionsService;
import com.example.pentagon.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


@Log4j2
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService userService;
    private final AdminSubscriptionsService subscriptionsService;
    private final AdminInquiryService inquiryService;

    // ✅ 회원 목록 (페이징)
    @GetMapping
    public Page<AdminUserListDTO> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.getAllUsers(page, size);
    }

    // ✅ 회원 검색 (페이징)
    @GetMapping("/search")
    public Page<AdminUserListDTO> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.searchUsers(keyword, page, size);
    }

    // ✅ 회원 상세
    @GetMapping("/detail/{id}")
    public AdminUserDetailDTO getAdminUserDetail(@PathVariable Long id) {
        return subscriptionsService.getAdminUserDetail(id);
    }

    // ✅ 문의 목록 (상태별, 페이징)
    @GetMapping("/inquiries")
    public Page<AdminInquiryListDTO> getInquiriesByStatus(
            @RequestParam InquiryStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return inquiryService.getList(status, page, size);
    }

    // ✅ 특정 유저 + 상태별 문의 (페이징)
    @GetMapping("/{userId}/inquiries")
    public Page<AdminInquiryListDTO> getInquiriesByUserAndStatus(
            @PathVariable Long userId,
            @RequestParam InquiryStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return inquiryService.getListByUser(userId, status, page, size);
    }

    // ✅ 전체 문의 (페이징)
    @GetMapping("/inquiries/all")
    public Page<AdminInquiryListDTO> getAllInquiries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return inquiryService.getAll(page, size);
    }

    // ✅ 특정 유저 전체 문의 (페이징)
    @GetMapping("/{userId}/inquiries/all")
    public Page<AdminInquiryListDTO> getAllInquiriesByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return inquiryService.getByUser(userId, page, size);
    }

    // ✅ 문의 상세
    @GetMapping("/inquiries/detail/{inquiryId}")
    public AdminInquiryDetailDTO getInquiryDetail(@PathVariable Long inquiryId) {
        return inquiryService.getDetail(inquiryId);
    }

    // ✅ 관리자: active 변경(동결/해제)
    // 예) /api/admin/users/5/active?active=false  (동결)
    // 예) /api/admin/users/5/active?active=true   (해제)
    @PutMapping("/{id}/active")
    public void setActive(@PathVariable Long id, @RequestParam boolean active) {
        userService.setActive(id, active);
    }

}
