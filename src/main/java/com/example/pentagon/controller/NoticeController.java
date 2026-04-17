package com.example.pentagon.controller;

import com.example.pentagon.domain.community.Post;
import com.example.pentagon.domain.enums.NoticeType;
import com.example.pentagon.domain.enums.PostStatus;
import com.example.pentagon.domain.enums.PostType;
import com.example.pentagon.dto.admin.AdminNoticeItemResponse;
import com.example.pentagon.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

    private final PostRepository postRepository;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    // ✅ 네 enum 이름에 맞춰 여기만 맞추면 됨
    private NoticeType toNoticeType(String cat) {
        return switch (cat) {
            case "emergency" -> NoticeType.URGENT; // 또는 URGENT (네 프로젝트에 맞게)
            case "event" -> NoticeType.EVENT;
            case "normal" -> NoticeType.GENERAL;
            default -> throw new IllegalArgumentException("Unknown cat: " + cat);
        };
    }

    private String toCat(NoticeType t) {
        return switch (t) {
            case URGENT -> "emergency"; // 또는 URGENT -> "emergency"
            case EVENT -> "event";
            case GENERAL -> "normal";
        };
    }

    @GetMapping
    public List<AdminNoticeItemResponse> list(@RequestParam String cat) {
        NoticeType nt = toNoticeType(cat);

        // ✅ 유저에게는 정상 공지만 노출 (HIDDEN/DELETED 제외)
        List<Post> list = postRepository
                .findByPostTypeAndNoticeTypeAndStatusOrderByCreatedAtDesc(
                        PostType.NOTICE, nt, PostStatus.NORMAL
                );

        return list.stream().map(p -> AdminNoticeItemResponse.builder()
                .id(p.getId())
                .cat(toCat(p.getNoticeType()))
                .title(p.getTitle())
                .content(p.getContent())
                .date(p.getCreatedAt() != null ? p.getCreatedAt().format(FMT) : "")
                .build()
        ).toList();
    }
}
