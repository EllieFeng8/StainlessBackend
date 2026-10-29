package com.example.demo.service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/** 專責後台與人機（HMI）的 HTTP 通訊，不處理工單或資料庫更新。 */
@Service
public class HmiCommunicationService {
    public enum MachineType { HEADING, THREADING }

    private final URI targetQuantityUri;
    private final RestClient client;

    public HmiCommunicationService(
            @Value("${hmi.target-quantity-url:}") String targetQuantityUrl,
            @Value("${hmi.connect-timeout-ms:3000}") long connectTimeoutMs,
            @Value("${hmi.read-timeout-ms:5000}") long readTimeoutMs) {
        if (connectTimeoutMs <= 0 || readTimeoutMs <= 0) {
            throw new IllegalArgumentException("HMI timeouts must be positive");
        }
        targetQuantityUri = endpoint(targetQuantityUrl);
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        var factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        client = RestClient.builder().requestFactory(factory).build();
    }

    /**
     * 下發目標產量。正常返回僅代表人機 API 回覆 2xx，不代表畫面已更新。
     * 逾時時無法判斷設備是否已收到，因此不自動重送。
     */
    public void setTargetQuantity(MachineType machineType, String machineCode, BigDecimal targetQuantity) {
        if (machineType == null || machineCode == null || machineCode.isBlank()
                || targetQuantity == null || targetQuantity.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "machineType, machineCode and a nonnegative targetQuantity are required");
        }
        if (targetQuantityUri == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "HMI endpoint is not configured: hmi.target-quantity-url");
        }
        try {
            client.post().uri(targetQuantityUri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("machineType", machineType.name(),
                            "machineCode", machineCode, "targetQuantity", targetQuantity))
                    .exchange((request, response) -> {
                        if (!response.getStatusCode().is2xxSuccessful()) {
                            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                                    "HMI rejected target quantity (HTTP " + response.getStatusCode().value() + ")");
                        }
                        return null;
                    });
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "HMI communication failed; delivery is unconfirmed", ex);
        }
    }

    private static URI endpoint(String value) {
        if (value == null || value.isBlank()) return null;
        URI uri = URI.create(value.trim());
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("hmi.target-quantity-url must be an absolute HTTP(S) URL without credentials or fragment");
        }
        return uri;
    }
}
