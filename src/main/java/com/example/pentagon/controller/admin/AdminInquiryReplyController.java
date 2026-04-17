package com.example.pentagon.controller.admin;


import com.example.pentagon.config.MyUserDetails;
import com.example.pentagon.service.admin.AdminInquiryReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inquiries/{inquiryId}/reply")
@RequiredArgsConstructor
public class AdminInquiryReplyController {

    private final AdminInquiryReplyService inquiryReplyService;

    @PostMapping
    public ResponseEntity<Long> insert(
            @PathVariable Long inquiryId,
            @RequestParam String content,
            @AuthenticationPrincipal MyUserDetails principal
    ) {
        Long adminId = principal.getUser().getId();
        Long replyId = inquiryReplyService.insertReply(inquiryId, adminId, content);
        return ResponseEntity.ok(replyId);
    }

    @PutMapping
    public ResponseEntity<Void> update(
            @PathVariable Long inquiryId,
            @RequestParam String content
    ) {
        inquiryReplyService.updateReply(inquiryId, content);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long inquiryId) {
        inquiryReplyService.deleteReply(inquiryId);
        return ResponseEntity.ok().build();
    }
}
