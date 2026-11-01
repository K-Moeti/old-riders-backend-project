package com.app.old_riders_bk.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class BulkSmsService {

    private final WebClient webClient;

    @Value("${bulksms.token.id:}")
    private String tokenId;

    @Value("${bulksms.token.secret:}")
    private String tokenSecret;

    @Value("${bulksms.enabled:true}")
    private boolean enabled;

    public BulkSmsService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Async
    public void sendLoginOtpSms(String rawPhoneNumber, String otpCode) {
        String formattedNumber = normalizePhoneNumber(rawPhoneNumber);
        String messageBody = "Your Old Riders verification code is: " + otpCode + ". Valid for 5 minutes.";

        String cleanId = tokenId != null ? tokenId.trim() : "";
        String cleanSecret = tokenSecret != null ? tokenSecret.trim() : "";

        if (!enabled || cleanId.isBlank() || cleanSecret.isBlank()) {
            log.warn("[BULKSMS] Service disabled or missing keys. ID length: {}, Secret length: {}",
                    cleanId.length(), cleanSecret.length());
            return;
        }

        String endpoint = "https://api.bulksms.com/v1/messages";

        Map<String, Object> payload = Map.of(
                "to", formattedNumber,
                "body", messageBody
        );

        String rawAuth = cleanId + ":" + cleanSecret;
        String encodedAuth = "Basic " + Base64.getEncoder().encodeToString(rawAuth.getBytes(StandardCharsets.UTF_8));

        log.info("Dispatching SMS via BulkSMS to {} (Token ID prefix: {}...)",
                formattedNumber, cleanId.length() > 4 ? cleanId.substring(0, 4) : "N/A");

        try {
            this.webClient.post()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, encodedAuth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> log.info("BulkSMS dispatch succeeded: {}", response),
                            error -> log.error("BulkSMS dispatch failed for {}: {}", formattedNumber, error.getMessage())
                    );
        } catch (Exception ex) {
            log.error("Failed to build BulkSMS request: {}", ex.getMessage());
        }
    }

    private String normalizePhoneNumber(String phone) {
        if (phone == null) return "";
        String clean = phone.replaceAll("[\\s\\-\\(\\)]", "");
        if (clean.startsWith("0")) {
            return "+27" + clean.substring(1);
        }
        if (!clean.startsWith("+")) {
            return "+" + clean;
        }
        return clean;
    }
}