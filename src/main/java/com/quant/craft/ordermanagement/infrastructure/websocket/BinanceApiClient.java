package com.quant.craft.ordermanagement.infrastructure.websocket;

import com.quant.craft.ordermanagement.domain.exchange.ExchangeType;
import com.quant.craft.ordermanagement.common.exception.ErrorCode;
import com.quant.craft.ordermanagement.common.exception.ExchangeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BinanceApiClient {
    private final RestTemplate restTemplate;

    @Value("${binance.api.base-url}")
    private String baseUrl;

    private static final String LISTEN_KEY_ENDPOINT = "/fapi/v1/listenKey";
    private static final String API_KEY_HEADER = "X-MBX-APIKEY";

    public String createListenKey(String apiKey) {
        ResponseEntity<Map> response = executeRequest(HttpMethod.POST, apiKey);
        return extractListenKey(response);
    }

    public void keepAliveListenKey(String apiKey) {
        executeRequest(HttpMethod.PUT, apiKey);
    }

    private ResponseEntity<Map> executeRequest(HttpMethod method, String apiKey) {
        HttpEntity<String> entity = new HttpEntity<>(createHeaders(apiKey));

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + LISTEN_KEY_ENDPOINT,
                    method,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.EXCHANGE_API_ERROR,
                        "API 요청 실패. 상태 코드: " + response.getStatusCode());
            }

            return response;
        } catch (Exception e) {
            throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.EXCHANGE_CONNECTION_ERROR,
                    "API 요청 중 오류 발생: " + e.getMessage());
        }
    }

    private static HttpHeaders createHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(API_KEY_HEADER, apiKey);
        return headers;
    }

    private String extractListenKey(ResponseEntity<Map> response) {
        Map<String, String> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("listenKey")) {
            return responseBody.get("listenKey");
        } else {
            throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.EXCHANGE_PARSING_ERROR,
                    "ListenKey를 찾을 수 없습니다.");
        }
    }
}