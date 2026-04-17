package com.example.pentagon.domain.reservation;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipt")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Receipt {

    @Id
    @Column(name = "receipt_id", length = 50)
    private String receiptId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    @Column(name = "issued_at", nullable = false)
    @Builder.Default
    private LocalDateTime issuedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (issuedAt == null) issuedAt = LocalDateTime.now();
    }
}
