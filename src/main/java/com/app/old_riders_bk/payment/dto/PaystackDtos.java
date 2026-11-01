package com.app.old_riders_bk.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

public class PaystackDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InitializePaymentRequest {
        private String email;
        private Long amount; // In cents (e.g. R120.00 = 12000)
        private String currency; // "ZAR"
        private String reference;
        @JsonProperty("callback_url")
        private String callbackUrl;
        private Map<String, Object> metadata;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InitializePaymentResponse {
        private boolean status;
        private String message;
        private Data data;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Data {
            @JsonProperty("authorization_url")
            private String authorizationUrl;

            @JsonProperty("access_code")
            private String accessCode;

            private String reference;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifyPaymentResponse {
        private boolean status;
        private String message;
        private VerifyData data;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class VerifyData {
            private Long id;
            private String status; // "success", "failed", "abandoned"
            private String reference;
            private Long amount;
            private String currency;
            @JsonProperty("channel")
            private String channel;
            @JsonProperty("gateway_response")
            private String gatewayResponse;
            private Map<String, Object> metadata;
        }
    }
}