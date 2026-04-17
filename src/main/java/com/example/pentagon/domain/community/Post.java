package com.example.pentagon.domain.community;

import com.example.pentagon.domain.User;
import com.example.pentagon.domain.enums.NoticeType;
import com.example.pentagon.domain.enums.PostStatus;
import com.example.pentagon.domain.enums.PostType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts",
        indexes = {
                @Index(name = "idx_posts_type", columnList = "post_type,notice_type"),
                @Index(name = "idx_posts_user", columnList = "user_id")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "user")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_posts_user_id"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 20)
    private PostType postType;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", length = 20)
    private NoticeType noticeType; // NOTICE일 때만 사용

    @Column(length = 255)
    private String title;

    @Lob
    private String content;

    private int likedCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void change(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void updateLikedCount(boolean add){
        if(add) this.likedCount++;
        else{
            if(this.likedCount > 0) this.likedCount--;
        }
    }
}
