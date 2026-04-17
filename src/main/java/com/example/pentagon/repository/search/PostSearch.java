package com.example.pentagon.repository.search;

import com.example.pentagon.domain.community.Post;
import com.example.pentagon.domain.enums.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostSearch {

    Page<Post> search1(Pageable pageable);

    Page<Post> searchAll(PostType postTYpe, String[] types, String keyword, Pageable pageable);
}
