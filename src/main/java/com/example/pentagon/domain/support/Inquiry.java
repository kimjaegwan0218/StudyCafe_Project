package com.example.pentagon.domain.support;

import com.example.pentagon.domain.User;
import com.example.pentagon.domain.enums.InquiryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiries",
        indexes = @Index(name = "idx_inquiries_user", columnList = "user_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_inquiries_user_id"))
    private User user;

    @Column(length = 255)
    private String title;

    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
