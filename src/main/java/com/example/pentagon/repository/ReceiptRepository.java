package com.example.pentagon.repository;

import com.example.pentagon.domain.reservation.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, String> {

    /**
     * 결제아이디를 영수증으로 검색決済IDで領収書を検索
     */

    Optional<Receipt> findByPayment_PaymentId(Long paymentId);
}
