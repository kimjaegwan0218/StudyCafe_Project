package com.example.pentagon.domain.community;

import com.example.pentagon.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_likes",
        indexes = @Index(name = "idx_post_likes_post", columnList = "post_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PostLike {

    @EmbeddedId
    private PostLikeId id;

    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_post_likes_post_id"))
    private Post post;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_post_likes_user_id"))
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
