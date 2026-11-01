package com.app.old_riders_bk.driver.dto;

import com.app.old_riders_bk.driver.entity.DriverStatus;
import com.app.old_riders_bk.driver.entity.VehicleFeature;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class DriverDtos {

    // ==========================================
    // --- 1. REGISTRATION & AUTH REQUESTS ---
    // ==========================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverRegisterRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Mobile number is required")
        private String mobileNumber;

        @NotBlank(message = "Email address is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        // Address
        @NotBlank(message = "Street address is required")
        private String streetAddress;

        @NotBlank(message = "Suburb is required")
        private String suburb;

        @NotBlank(message = "City or town is required")
        private String cityOrTown;

        @NotBlank(message = "Postal code is required")
        private String postalCode;

        // Mandatory Documents
        @NotBlank(message = "ID document is required")
        private String idDocumentUrl;

        @NotBlank(message = "Driver licence front is required")
        private String driversLicenceFrontUrl;

        @NotBlank(message = "Driver licence back is required")
        private String driversLicenceBackUrl;

        @NotBlank(message = "Vehicle licence disc is required")
        private String vehicleLicenceDiscUrl;

        @NotNull(message = "Vehicle licence disc expiry date is required")
        @Future(message = "Licence disc expiry date must be in the future")
        private LocalDate licenceDiscExpiryDate;

        // Optional Roadworthy
        private String roadworthyCertificateUrl;
        private LocalDate roadworthyExpiryDate;

        // Optional Insurance
        private String insuranceDocumentUrl;
        private LocalDate insuranceExpiryDate;

        // Terms
        @NotNull(message = "You must agree to the terms and conditions")
        @AssertTrue(message = "Terms and conditions must be accepted")
        private Boolean agreedToTerms;

        // Vehicle Info
        @NotBlank(message = "Vehicle model is required")
        private String vehicleModel;

        @NotBlank(message = "Vehicle color is required")
        private String vehicleColor;

        @NotBlank(message = "Registration plate is required")
        private String registrationPlate;

        @NotNull(message = "Seating capacity is required")
        @Min(value = 1, message = "Vehicle must have at least 1 passenger seat")
        @Max(value = 15, message = "Seating capacity cannot exceed 15")
        private Integer seatingCapacity;

        @NotNull(message = "Please specify if vehicle is wheelchair friendly")
        private Boolean isWheelchairFriendly;

        private Set<VehicleFeature> vehicleFeatures;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverLoginRequest {
        @NotBlank(message = "Email or mobile number is required")
        private String emailOrPhone;

        @NotBlank(message = "Password is required")
        private String password;
    }

    // ==========================================
    // --- 2. DRIVER PROFILE CRUD REQUESTS ---
    // ==========================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDriverProfileRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Mobile number is required")
        private String mobileNumber;

        @NotBlank(message = "Email address is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Street address is required")
        private String streetAddress;

        @NotBlank(message = "Suburb is required")
        private String suburb;

        @NotBlank(message = "City or town is required")
        private String cityOrTown;

        @NotBlank(message = "Postal code is required")
        private String postalCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeDriverPasswordRequest {
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password must be at least 6 characters")
        private String newPassword;
    }

    // ==========================================
    // --- 3. "MY VEHICLE" CRUD REQUESTS ---
    // ==========================================

    // CREATE / FULL UPDATE (PUT)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveMyVehicleRequest {
        @NotBlank(message = "Vehicle model is required")
        private String vehicleModel;

        @NotBlank(message = "Vehicle color is required")
        private String vehicleColor;

        @NotBlank(message = "Registration plate is required")
        private String registrationPlate;

        @NotNull(message = "Seating capacity is required")
        @Min(value = 1, message = "Vehicle must have at least 1 passenger seat")
        @Max(value = 15, message = "Seating capacity cannot exceed 15")
        private Integer seatingCapacity;

        @NotNull(message = "Wheelchair accessibility must be specified")
        private Boolean isWheelchairFriendly;

        private Set<VehicleFeature> vehicleFeatures;

        // Mandatory Licence Disc
        @NotBlank(message = "Vehicle licence disc URL is required")
        private String vehicleLicenceDiscUrl;

        @NotNull(message = "Licence disc expiry date is required")
        private LocalDate licenceDiscExpiryDate;

        // Optional Roadworthy & Insurance
        private String roadworthyCertificateUrl;
        private LocalDate roadworthyExpiryDate;
        private String insuranceDocumentUrl;
        private LocalDate insuranceExpiryDate;
    }

    // PATCH: Update Vehicle Specifications Only (Color, Seats, Wheelchair, Plate)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatchVehicleSpecsRequest {
        private String vehicleModel;
        private String vehicleColor;
        private String registrationPlate;
        private Integer seatingCapacity;
        private Boolean isWheelchairFriendly;
    }

    // PATCH: Toggle / Update Vehicle Features Only
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateVehicleFeaturesRequest {
        @NotNull(message = "Vehicle features set is required")
        private Set<VehicleFeature> vehicleFeatures;
    }

    // PATCH: Renew / Update Vehicle Compliance Documents & Expiry Dates Only
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateVehicleComplianceRequest {
        private String vehicleLicenceDiscUrl;
        private LocalDate licenceDiscExpiryDate;
        private String roadworthyCertificateUrl;
        private LocalDate roadworthyExpiryDate;
        private String insuranceDocumentUrl;
        private LocalDate insuranceExpiryDate;
    }

    // ==========================================
    // --- 4. DRIVER LICENCE / ID RENEWAL ---
    // ==========================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDriverDocumentsRequest {
        @NotBlank(message = "ID document is required")
        private String idDocumentUrl;

        @NotBlank(message = "Driver licence front is required")
        private String driversLicenceFrontUrl;

        @NotBlank(message = "Driver licence back is required")
        private String driversLicenceBackUrl;

        @NotBlank(message = "Vehicle licence disc is required")
        private String vehicleLicenceDiscUrl;

        @NotNull(message = "Licence disc expiry date is required")
        private LocalDate licenceDiscExpiryDate;

        // Optional Roadworthy & Insurance
        private String roadworthyCertificateUrl;
        private LocalDate roadworthyExpiryDate;
        private String insuranceDocumentUrl;
        private LocalDate insuranceExpiryDate;
    }

    // ==========================================
    // --- 5. RESPONSES ---
    // ==========================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyVehicleResponse {
        private Long driverId;
        private String vehicleModel;
        private String vehicleColor;
        private String registrationPlate;
        private Integer seatingCapacity;
        private Boolean isWheelchairFriendly;
        private Set<VehicleFeature> vehicleFeatures;

        // Compliance documents & dates
        private String vehicleLicenceDiscUrl;
        private LocalDate licenceDiscExpiryDate;
        private Boolean isLicenceDiscExpired;

        private String roadworthyCertificateUrl;
        private LocalDate roadworthyExpiryDate;
        private Boolean isRoadworthyExpired;
        private Boolean hasRoadworthy;

        private String insuranceDocumentUrl;
        private LocalDate insuranceExpiryDate;
        private Boolean hasInsurance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverResponse {
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

        // Documents & compliance
        private String idDocumentUrl;
        private String driversLicenceFrontUrl;
        private String driversLicenceBackUrl;
        private String vehicleLicenceDiscUrl;
        private LocalDate licenceDiscExpiryDate;
        private String roadworthyCertificateUrl;
        private LocalDate roadworthyExpiryDate;
        private String insuranceDocumentUrl;
        private LocalDate insuranceExpiryDate;

        private Boolean agreedToTerms;
        private Double rating;
        private DriverStatus status;
        private Boolean isOnline;
        private LocalDateTime createdAt;
    }
}