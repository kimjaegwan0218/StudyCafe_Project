// com.example.pentagon.service.admin.AdminNoticeServiceImpl
package com.example.pentagon.service.admin;

import com.example.pentagon.domain.User;
import com.example.pentagon.domain.community.Post;
import com.example.pentagon.domain.enums.NoticeType;
import com.example.pentagon.domain.enums.PostStatus;
import com.example.pentagon.domain.enums.PostType;
import com.example.pentagon.dto.admin.AdminNoticeItemResponse;
import com.example.pentagon.dto.admin.AdminNoticeUpsertRequest;
import com.example.pentagon.repository.PostRepository;
import com.example.pentagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNoticeServiceImpl implements AdminNoticeService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private NoticeType toNoticeType(String cat) {
        if (cat == null) return null;
        return switch (cat) {
            case "emergency" -> NoticeType.URGENT;
            case "event" -> NoticeType.EVENT;
            case "normal" -> NoticeType.GENERAL;
            default -> throw new IllegalArgumentException("Unknown cat: " + cat);
        };
    }

    private String toCat(NoticeType t) {
        if (t == null) return "normal";
        return switch (t) {
            case URGENT -> "emergency";
            case EVENT -> "event";
            case GENERAL -> "normal";
        };
    }

    private AdminNoticeItemResponse toRes(Post p) {
        return AdminNoticeItemResponse.builder()
                .id(p.getId())
                .cat(toCat(p.getNoticeType()))
                .title(p.getTitle())
                .content(p.getContent())
                .date(p.getCreatedAt() != null ? p.getCreatedAt().format(FMT) : "")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminNoticeItemResponse> list(String cat) {
        NoticeType nt = (cat == null || cat.isBlank()) ? null : toNoticeType(cat);
        List<Post> list = (nt == null)
                ? postRepository.findByPostTypeOrderByCreatedAtDesc(PostType.NOTICE)
                : postRepository.findByPostTypeAndNoticeTypeOrderByCreatedAtDesc(PostType.NOTICE, nt);

        return list.stream().map(this::toRes).toList();
    }

    @Override
    @Transactional
    public AdminNoticeItemResponse create(Long adminUserId, AdminNoticeUpsertRequest req) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("admin user not found"));

        Post post = Post.builder()
                .user(admin)
                .postType(PostType.NOTICE)
                .noticeType(toNoticeType(req.getCat()))
                .title(req.getTitle())
                .content(req.getContent())
                .likedCount(0)
                .status(PostStatus.NORMAL) // ✅ 너 enum에 맞게 바꿔
                .build();

        return toRes(postRepository.save(post));
    }

    @Override
    @Transactional
    public AdminNoticeItemResponse update(Long adminUserId, Long id, AdminNoticeUpsertRequest req) {
        Post post = postRepository.findByIdAndPostType(id, PostType.NOTICE)
                .orElseThrow(() -> new IllegalArgumentException("notice not found"));

        post.setNoticeType(toNoticeType(req.getCat()));
        post.change(req.getTitle(), req.getContent());
        return toRes(post);
    }

    @Override
    @Transactional
    public void delete(Long adminUserId, Long id) {
        Post post = postRepository.findByIdAndPostType(id, PostType.NOTICE)
                .orElseThrow(() -> new IllegalArgumentException("notice not found"));

        // 1) 소프트 삭제가 있으면:
        post.setStatus(PostStatus.DELETED);
        // 2) 하드 삭제로 갈 거면 위 줄 대신
        postRepository.delete(post);
    }


}
