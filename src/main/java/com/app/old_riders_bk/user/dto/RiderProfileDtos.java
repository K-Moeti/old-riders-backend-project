package com.app.old_riders_bk.user.dto;

import com.app.old_riders_bk.user.entity.MobilityAssistance;
import com.app.old_riders_bk.user.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

public class RiderProfileDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRiderProfileRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Phone number is required")
        private String phoneNumber;

        @NotBlank(message = "Emergency phone number is required")
        private String emergencyPhoneNumber;

        private String emergencyContactName;

        // Home Address
        private String street;
        private String homeNumber;
        private String province;
        private String postalCode;

        // How can we help you ride (Mobility needs)
        private Set<MobilityAssistance> mobilityNeeds;

        // Safety feature toggle
        @NotNull(message = "Share ride with family flag is required")
        private Boolean shareRideWithFamily;

        // Payment preference
        @NotNull(message = "Preferred payment method is required")
        private PaymentMethod preferredPaymentMethod;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToggleShareRideRequest {
        @NotNull(message = "Share ride status is required")
        private Boolean shareRideWithFamily;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePaymentMethodRequest {
        @NotNull(message = "Preferred payment method is required")
        private PaymentMethod preferredPaymentMethod;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateMobilityNeedsRequest {
        private Set<MobilityAssistance> mobilityNeeds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiderProfileResponse {
        private Long id;
        private String fullName;
        private String phoneNumber;
        private String emergencyPhoneNumber;
        private String emergencyContactName;
        private String street;
        private String homeNumber;
        private String province;
        private String postalCode;
        private Set<MobilityAssistance> mobilityNeeds;
        private Boolean shareRideWithFamily;
        private PaymentMethod preferredPaymentMethod;
        private Boolean isDisabled;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}