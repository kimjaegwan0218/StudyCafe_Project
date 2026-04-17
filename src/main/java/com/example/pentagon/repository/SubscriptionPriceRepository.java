package com.example.pentagon.repository;

import com.example.pentagon.domain.reservation.SubscriptionPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 구독요금repositoryサブスクリプション料金リポジトリ
 *
 * 배치장소配置場所: repository/SubscriptionPriceRepository.java
 */
@Repository
public interface SubscriptionPriceRepository extends JpaRepository<SubscriptionPrice, String> {

    /**
     * 플랜타입으로 요금검색プランタイプで料金を検索
     */
    Optional<SubscriptionPrice> findBySubscriptionType(String subscriptionType);

    /**
     * 유효한 플랜만 취득有効なプランのみ取得
     */
    List<SubscriptionPrice> findByActive(Integer active);

    /**
     * 유효한 플랜을 일자순으로 취득有効なプランを日数順（昇順）で取得
     */
    List<SubscriptionPrice> findByActiveOrderByDurationDaysAsc(Integer active);
}
