package com.app.old_riders_bk.user.dto;

import com.app.old_riders_bk.user.entity.MobilityAssistance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

public class RiderDtos {

    // --- 1. Rider Registration Request ---
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiderRegisterRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^(\\+27|0)[6-8][0-9]{8}$",
                message = "Invalid South African phone number format. Use 08XXXXXXXX or +278XXXXXXXX"
        )
        private String phoneNumber;

        @NotBlank(message = "Emergency phone number is required")
        @Pattern(
                regexp = "^(\\+27|0)[6-8][0-9]{8}$",
                message = "Invalid emergency contact phone number format"
        )
        private String emergencyPhoneNumber;

        @NotBlank(message = "Emergency contact name is required")
        private String emergencyContactName;

        private String street;
        private String homeNumber;
        private String province;
        private String postalCode;
        private Set<MobilityAssistance> mobilityNeeds;
    }

    // --- 2. Request OTP for Login / Verification ---
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestLoginOtpRequest {
        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^(\\+27|0)[6-8][0-9]{8}$",
                message = "Invalid South African phone number format"
        )
        private String phoneNumber;
    }

    // --- 3. Resend OTP Request ---
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResendOtpRequest {
        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^(\\+27|0)[6-8][0-9]{8}$",
                message = "Invalid South African phone number format"
        )
        private String phoneNumber;
    }

    // --- 4. Verify OTP to Complete Login ---
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyOtpRequest {
        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^(\\+27|0)[6-8][0-9]{8}$",
                message = "Invalid South African phone number format"
        )
        private String phoneNumber;

        @NotBlank(message = "OTP code is required")
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP code must be exactly 6 digits")
        private String otpCode;
    }

    // --- 5. OTP Response (Sent after requesting OTP) ---
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtpResponse {
        private String message;
        private String phoneNumber;
        private Integer expiresInSeconds;
        private LocalDateTime expiresAt;
        private String debugOtpCode; // Retained for dev/test environments before live SMS gateway
    }

    // --- 6. Auth Success Response (Returned after OTP verification) ---
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthSuccessResponse {
        private String token;
        private String tokenType;
        private Long riderId;
        private String fullName;
        private String phoneNumber;
        private Boolean isProfileComplete;
        private String message;
    }
}