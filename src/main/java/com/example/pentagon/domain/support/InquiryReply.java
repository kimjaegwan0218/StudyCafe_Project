package com.example.pentagon.domain.support;

import com.example.pentagon.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry_replies",
        indexes = @Index(name = "idx_inquiry_replies_inquiry", columnList = "inquiry_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class InquiryReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_inquiry_replies_inquiry_id"))
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_inquiry_replies_user_id"))
    private User user;

    @Lob
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
