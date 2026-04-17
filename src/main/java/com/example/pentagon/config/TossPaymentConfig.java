package com.example.pentagon.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Toss Payments 설정
 */

@Configuration
@ConfigurationProperties(prefix = "toss.payments")
@Getter
@Setter
public class TossPaymentConfig {

    /**
     * Toss 클라이언트key（프론트에서사용）
     * 例: test_ck_xxxxxxxxxxxxxxx
     */
    private String clientKey;

    /**
     * Toss 시크릿key（서버에서사용）
     * 例: test_sk_xxxxxxxxxxxxxxx
     */
    private String secretKey;

    /**
     * 결제성공시 리다이렉트URL
     */
    private String successUrl;

    /**
     * 결제실패시 리다이렉트URL
     */
    private String failUrl;

    /**
     * Toss API 의 페이지URL
     */
    private static final String TOSS_API_URL = "https://api.tosspayments.com/v1";

    /**
     * 결제확인API URL
     */
    public String getConfirmUrl() {
        return TOSS_API_URL + "/payments/confirm";

    }

    /**
     * 결제 캔슬API URL
     */
    public String getCancelUrl(String paymentKey) {
        return TOSS_API_URL + "/payments/" + paymentKey + "/cancel";
    }

    /**
     * RestTemplate Bean（HTTP통신용）
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }




}
