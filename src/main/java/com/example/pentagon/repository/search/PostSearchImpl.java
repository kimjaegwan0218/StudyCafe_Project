package com.example.pentagon.repository.search;

import com.example.pentagon.domain.community.Post;
import com.example.pentagon.domain.community.QPost;
import com.example.pentagon.domain.enums.PostType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.example.pentagon.domain.community.QPost.post;

public class PostSearchImpl extends QuerydslRepositorySupport implements PostSearch {

    public PostSearchImpl() {
        super(Post.class);
    }

    @Override
    public Page<Post> search1(Pageable pageable) {

        QPost Post = QPost.post;

        JPQLQuery<Post> query = from(Post);

        query.where(post.title.contains("1"));

        //paging
        this.getQuerydsl().applyPagination(pageable, query);

        List<Post> posts = query.fetch();

        long count = query.fetchCount();

        return null;
    }

    @Override
    public Page<Post> searchAll(PostType postType, String[] types, String keyword, Pageable pageable) {

        QPost post = QPost.post;
        JPQLQuery<Post> query = from(post);

        // ✅ 핵심: postType 필터
        if (postType != null) {
            query.where(post.postType.eq(postType));
        }

        if ((types != null && types.length > 0) && keyword != null) {

            BooleanBuilder booleanBuilder = new BooleanBuilder();

            for (String type : types) {
                switch (type) {
                    case "t" -> booleanBuilder.or(post.title.contains(keyword));
                    case "c" -> booleanBuilder.or(post.content.contains(keyword));
                    case "u" -> booleanBuilder.or(post.user.name.contains(keyword));
                }
            }
            query.where(booleanBuilder);
        }

        query.where(post.id.gt(0L));

        this.getQuerydsl().applyPagination(pageable, query);

        List<Post> list = query.fetch();
        long count = query.fetchCount();

        return new PageImpl<>(list, pageable, count);
    }

}
