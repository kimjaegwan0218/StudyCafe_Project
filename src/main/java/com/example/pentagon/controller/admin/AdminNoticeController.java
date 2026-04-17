// com.example.pentagon.controller.admin.AdminNoticeApiController
package com.example.pentagon.controller.admin;

import com.example.pentagon.config.MyUserDetails;
import com.example.pentagon.dto.admin.AdminNoticeItemResponse;
import com.example.pentagon.dto.admin.AdminNoticeUpsertRequest;
import com.example.pentagon.service.admin.AdminNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notices")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    @GetMapping
    public List<AdminNoticeItemResponse> list(@RequestParam(required = false) String cat) {
        return adminNoticeService.list(cat);
    }

    @PostMapping
    public AdminNoticeItemResponse create(@AuthenticationPrincipal MyUserDetails me,
                                          @RequestBody AdminNoticeUpsertRequest req) {
        return adminNoticeService.create(me.getUserId(), req);
    }

    @PutMapping("/{id}")
    public AdminNoticeItemResponse update(@AuthenticationPrincipal MyUserDetails me,
                                          @PathVariable Long id,
                                          @RequestBody AdminNoticeUpsertRequest req) {
        return adminNoticeService.update(me.getUserId(), id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal MyUserDetails me, @PathVariable Long id) {
        adminNoticeService.delete(me.getUserId(), id);
    }
}
