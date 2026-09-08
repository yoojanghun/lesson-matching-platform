package com.lessonmatchingplatform.lesson_matching_platform.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.PaymentConfirmRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.TossApproveResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.TossErrorResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.exception.TossPaymentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Component
public class TossPaymentsClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String secretKey;
    private final String apiUrl;

    public TossPaymentsClient(
            ObjectMapper objectMapper,
            @Value("${toss.payments.secret-key}") String secretKey,
            @Value("${toss.payments.confirm-url}") String apiUrl
    ) {
        this.objectMapper = objectMapper;
        this.secretKey = secretKey;
        this.apiUrl = apiUrl;

        // Timeout 설정으로 무한 대기 문제 방지 (연결 3초, 읽기 10초)
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());   // TCP 연결 시 3초까지만 기다림
        requestFactory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());     // 승인 결과 데이터를 받아올 때 10초까지만 기다림

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
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
                    try (InputStream is = res.getBody()) {
                        TossErrorResponse errorResponse = objectMapper.readValue(is, TossErrorResponse.class);
                        throw new TossPaymentException(
                                errorResponse.code() != null ? errorResponse.code() : "UNKNOWN_PAYMENT_ERROR",
                                errorResponse.message() != null ? errorResponse.message() : "PG사 결제 승인 요청에 실패했습니다."
                        );
                    } catch (TossPaymentException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new TossPaymentException("PAYMENT_SERVER_ERROR", "결제 승인 처리 중 오류가 발생했습니다.");
                    }
                })
                .body(TossApproveResponse.class);
    }
}
