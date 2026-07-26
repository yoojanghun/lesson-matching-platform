package com.lessonmatchingplatform.lesson_matching_platform.payment.client;

import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.PaymentConfirmRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.TossApproveResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TossPaymentsClient {

    private final RestClient restClient;
    private final String secretKey;
    private final String apiUrl;

    public TossPaymentsClient(
            @Value("${toss.payments.secret-key}") String secretKey,
            @Value("${toss.payments.confirm-url}") String apiUrl
    ) {
        this.secretKey = secretKey;
        this.apiUrl = apiUrl;
        this.restClient = RestClient.create();
    }

    public TossApproveResponse confirmPayment(PaymentConfirmRequest request) {
        // 1. Basic Auth 인증 헤더 인코딩 (SecretKey + ":")
        String authorizationHeader = "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        // 2. REST API 호출
        return restClient.post()
                .uri(apiUrl)
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
                    throw new IllegalArgumentException("PG사 결제 승인 요청에 실패했습니다.");
                })
                .body(TossApproveResponse.class);
    }
}
