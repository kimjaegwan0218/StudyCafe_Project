package com.example.pentagon.service;

import com.example.pentagon.config.TossPaymentConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Toss Payments API연계서비스連携サービス
 *
 * 배치장소配置場所: service/TossPaymentService.java
 *
 * Toss Payments API메뉴얼ドキュメント: https://docs.tosspayments.com/
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TossPaymentService {

    private final TossPaymentConfig tossConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 결제를확인(승인)하기決済を確認（承認）する
     *
     * 프론트에서 토스결제를 완료하면 successsurl에 리다이렉트됨フロントでToss決済が完了すると、successUrlにリダイレクトされる
     * 그때 이 메소드를 소환, 서버측에 결제를 최종확인한다.その時にこのメソッドを呼んで、サーバー側で決済を最終確認する
     *
     * @param paymentKey 토스가 발행하는 결제keyTossが発行した決済キー
     * @param orderId    이쪽에서 생성한 주문아이디こちらで生成した注文ID
     * @param amount     결제금액決済金額
     * @return 확인결과確認結果
     */
    public TossConfirmResult confirmPayment(String paymentKey, String orderId, int amount) {
        log.info("Toss결제확인개시: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);

        try {
            // 리퀘스트body작성リクエストボディ作成
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("paymentKey", paymentKey);
            requestBody.put("orderId", orderId);
            requestBody.put("amount", amount);

            // HTTP헤더설정(베이직인증)ヘッダー設定（Basic認証）
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Toss API소환呼び出し
            ResponseEntity<String> response = restTemplate.postForEntity(
                    tossConfig.getConfirmUrl(),
                    request,
                    String.class
            );

            // 레스폰스 해석レスポンス解析
            JsonNode responseJson = objectMapper.readTree(response.getBody());

            log.info("Toss결제확인성공: status={}", responseJson.get("status").asText());

            return TossConfirmResult.builder()
                    .success(true)
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .status(responseJson.get("status").asText())
                    .method(responseJson.has("method") ? responseJson.get("method").asText() : "CARD")
                    .totalAmount(responseJson.get("totalAmount").asInt())
                    .build();

        } catch (HttpClientErrorException e) {
            log.error("Toss결제확인실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());

            try {
                JsonNode errorJson = objectMapper.readTree(e.getResponseBodyAsString());
                return TossConfirmResult.builder()
                        .success(false)
                        .errorCode(errorJson.get("code").asText())
                        .errorMessage(errorJson.get("message").asText())
                        .build();
            } catch (Exception ex) {
                return TossConfirmResult.builder()
                        .success(false)
                        .errorMessage("결제확인에실패했쥬")
                        .build();
            }

        } catch (Exception e) {
            log.error("Toss결제확인에러: ", e);
            return TossConfirmResult.builder()
                    .success(false)
                    .errorMessage("결제처리중에에러가발생했어요: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 결제를 캔슬(환불)하기決済をキャンセル（払い戻し）する
     *
     * @param paymentKey 토스 결제 key Tossの決済キー
     * @param cancelReason 캔슬이유 キャンセル理由
     * @return 캔슬결과 キャンセル結果
     */
    public TossCancelResult cancelPayment(String paymentKey, String cancelReason) {
        log.info("Toss결제캔슬개시: paymentKey={}", paymentKey);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("cancelReason", cancelReason);

            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    tossConfig.getCancelUrl(paymentKey),
                    request,
                    String.class
            );

            JsonNode responseJson = objectMapper.readTree(response.getBody());

            log.info("Toss결제캔슬성공");

            return TossCancelResult.builder()
                    .success(true)
                    .cancelAmount(responseJson.get("cancels").get(0).get("cancelAmount").asInt())
                    .build();

        } catch (HttpClientErrorException e) {
            log.error("Toss결제캔슬실패: {}", e.getResponseBodyAsString());

            try {
                JsonNode errorJson = objectMapper.readTree(e.getResponseBodyAsString());
                return TossCancelResult.builder()
                        .success(false)
                        .errorCode(errorJson.get("code").asText())
                        .errorMessage(errorJson.get("message").asText())
                        .build();
            } catch (Exception ex) {
                return TossCancelResult.builder()
                        .success(false)
                        .errorMessage("캔슬에실패했어요")
                        .build();
            }

        } catch (Exception e) {
            log.error("Toss결제캔슬에러: ", e);
            return TossCancelResult.builder()
                    .success(false)
                    .errorMessage("캔슬처리중에에러가발생했어요")
                    .build();
        }
    }

    /**
     * Basic인증헤더를작성 認証ヘッダーを作成
     * Toss API는 시크릿key을 はシークレットキーをBase64 엔코드해서 エンコードしてAuthorization헤더에 설정ヘッダーに設定
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();

        // 시크릿 key + シークレットキー + ":" 를 base64엔코드をBase64エンコード
        String credentials = tossConfig.getSecretKey() + ":";
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        headers.set("Authorization", "Basic " + encodedCredentials);
        return headers;
    }

    // ============================================
    // 결과 오브젝트 結果オブジェクト
    // ============================================

    @lombok.Getter
    @lombok.Setter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TossConfirmResult {
        private boolean success;
        private String paymentKey;
        private String orderId;
        private String status;      // DONE, CANCELED, etc.
        private String method;      // CARD, 가상계좌, etc.
        private Integer totalAmount;
        private String errorCode;
        private String errorMessage;
    }

    @lombok.Getter
    @lombok.Setter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TossCancelResult {
        private boolean success;
        private Integer cancelAmount;
        private String errorCode;
        private String errorMessage;
    }
}