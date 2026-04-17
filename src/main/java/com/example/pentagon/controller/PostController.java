package com.example.pentagon.controller;


import com.example.pentagon.config.MyUserDetails;
import com.example.pentagon.dto.community.CommentDTO;
import com.example.pentagon.dto.community.PageRequestDTO;
import com.example.pentagon.dto.community.PageResponseDTO;
import com.example.pentagon.dto.community.PostDTO;
import com.example.pentagon.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
@Log4j2
public class PostController {

    private final PostService postService;

    @GetMapping({"", "/"})
    public String list(PageRequestDTO pageRequestDTO, Model model) {

        log.info("게시판 목록 조회 시작: " + pageRequestDTO);

        // 일반 게시글 목록 (페이징 + 검색 결과)
        PageResponseDTO<PostDTO> responseDTO = postService.list(pageRequestDTO);

        // 상단 인기 게시글 TOP 3 (좋아요 순)
        List<PostDTO> top3List = postService.getTop3Posts();

        // 화면(Thymeleaf)으로 데이터 전달
        model.addAttribute("responseDTO", responseDTO); // 리스트 영역
        model.addAttribute("top3List", top3List);// TOP 3 영역

        return "community/comlist";
    }

    @GetMapping("/register")
    public String registerGET(Model model) {
        log.info("게시글 등록 화면 이동.....");

        model.addAttribute("postForm", new PostDTO());

        return "community/register";
    }

    @PostMapping("/register")
    public String register(
            PostDTO postDTO,
            @AuthenticationPrincipal MyUserDetails userDetails
            ) {

        Long userId = userDetails.getUser().getId(); // 로그인 유저 ID

        postService.register(postDTO, userId);

        return "redirect:/community";
    }


    @GetMapping("/read/{id}")
    public String read(@PathVariable("id") Long id,
                       Model model,
                       @AuthenticationPrincipal MyUserDetails userDetails) {

        PostDTO postDTO = postService.readOne(id);
        model.addAttribute("post", postDTO);

        List<CommentDTO> comments = postService.getComment(id);
        model.addAttribute("comments", comments);

        if (userDetails != null) {
            model.addAttribute("loginUserId", userDetails.getUser().getId());
        }

        return "community/read";
    }


    @PostMapping("/like")
    public String likePost(Long postId, @AuthenticationPrincipal MyUserDetails userDetails) {
        // 1. 로그인하지 않은 사용자가 클릭했을 경우 처리 (보안 및 예외 방지)
        if (userDetails == null) {
            return "redirect:/login";
        }

        // 2. 고정된 '1L' 대신 실제 로그인한 유저의 ID를 추출합니다.
        Long userId = userDetails.getUser().getId();

        // 3. 서비스 호출 시 해당 유저의 ID를 전달하여 좋아요 로직을 실행합니다.
        postService.toggleLike(postId, userId);

        return "redirect:/community/read/" + postId;
    }

    // --- 1. 댓글 등록 부분 ---
    @PostMapping("/comment/register")
    public String registerComment(CommentDTO commentDTO, @AuthenticationPrincipal MyUserDetails userDetails) {
        postService.registerComment(commentDTO, userDetails.getUser().getId());

        return "redirect:/community/read/" + commentDTO.getPostId();
    }

    @PostMapping("/comment/remove/{id}")
    public String removeComment(@PathVariable("id") Long commentId,
                                @RequestParam("postId") Long postId,
                                @AuthenticationPrincipal MyUserDetails userDetails) {
        postService.removeComment(commentId, userDetails.getUser().getId());

        // redirectAttributes.addAttribute(...)는 지우고 아래처럼 수정
        return "redirect:/community/read/" + postId;
    }


    //수정 페이지로 보내주는 역할
    @GetMapping("/modify/{id}")
    public String modifyForm(@PathVariable("id") Long id, Model model) {
        PostDTO postDTO = postService.read(id);
        model.addAttribute("post", postDTO);
        return "community/modify"; // 여기서 templates/community/modify.html을 호출함
    }

    @PostMapping("/modify")
    public String modify(
            @ModelAttribute PostDTO postDTO,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        Long loginUserId = userDetails.getUser().getId();

        postService.modify(postDTO, loginUserId);

        return "redirect:/community/read/" + postDTO.getId();
    }

    // 게시글 삭제 처리 (댓글 자동 삭제는 DB의 Cascade 또는 Service 로직에서 처리)
    @PostMapping("/remove/{id}")
    public String remove(
            @PathVariable Long id,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        Long loginUserId = userDetails.getUser().getId();

        postService.removeWithComments(id, loginUserId);

        return "redirect:/community";
    }

}
