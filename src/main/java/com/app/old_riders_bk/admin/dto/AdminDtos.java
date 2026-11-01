package com.app.old_riders_bk.admin.dto;

import com.app.old_riders_bk.admin.entity.AdminRole;
import com.app.old_riders_bk.driver.entity.DriverStatus;
import com.app.old_riders_bk.driver.entity.VehicleFeature;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class AdminDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateAdminRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Valid email is required")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotNull(message = "Role is required (SUPER_ADMIN or ADMIN)")
        private AdminRole role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminLoginRequest {
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    // --- DTO for reviewing driver documents & compliance ---
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewDriverRequest {
        @NotNull(message = "Admin ID is required")
        private Long adminId;

        // Accepts APPROVED, REJECTED, or DOCUMENT_REUPLOAD_REQUESTED
        @NotNull(message = "Decision is required (APPROVED, REJECTED, or DOCUMENT_REUPLOAD_REQUESTED)")
        private DriverStatus decision;

        // Reason note mandatory if REJECTED or DOCUMENT_REUPLOAD_REQUESTED
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminResponse {
        private Long id;
        private String fullName;
        private String email;
        private AdminRole role;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverDocumentReviewCard {
        private Long driverId;
        private String fullName;
        private String mobileNumber;
        private String email;
        private String streetAddress;
        private String suburb;
        private String cityOrTown;
        private String postalCode;

        // Vehicle specifications
        private String vehicleModel;
        private String vehicleColor;
        private String registrationPlate;
        private Integer seatingCapacity;
        private Boolean isWheelchairFriendly;
        private Set<VehicleFeature> vehicleFeatures;

        // Driver identity & licence documents
        private String idDocumentUrl;
        private String driversLicenceFrontUrl;
        private String driversLicenceBackUrl;

        // Vehicle compliance documents & expiry dates
        private String vehicleLicenceDiscUrl;
        private LocalDate licenceDiscExpiryDate;

        private String roadworthyCertificateUrl;
        private LocalDate roadworthyExpiryDate;

        private String insuranceDocumentUrl;
        private LocalDate insuranceExpiryDate;

        // Verification audit details
        private Boolean agreedToTerms;
        private DriverStatus status;
        private String rejectionReason;
        private LocalDateTime submittedAt;
        private LocalDateTime reviewedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuperAdminDashboardStats {
        private Long totalRiders;
        private Long totalDrivers;
        private Long pendingApprovalDrivers;
        private Long approvedDrivers;
        private Long totalRides;
        private Long completedRides;
        private BigDecimal totalPlatformRevenue;
    }
}