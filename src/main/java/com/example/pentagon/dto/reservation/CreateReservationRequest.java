package com.example.pentagon.dto.reservation;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationRequest {
    private Long userId;           // 유저아이디ユーザーID
    private Long seatId;           // 좌석아이디座席ID
    private LocalDateTime startAt; // 개시일자開始日時
    private Integer durationHours; // 이용시간利用時間
    private Integer basePrice;     // 기본요금基本料金
    private Integer surchargePrice;// 추가요금追加料金
    private String subscriptionCode; // 구독코드(있다면)サブスクコード（あれば）
    private String couponCode;     // 쿠폰코드(있다면)クーポンコード（あれば）
}
