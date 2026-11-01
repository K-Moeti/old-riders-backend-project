package com.app.old_riders_bk.booking.dto;

import com.app.old_riders_bk.booking.entity.BookingStatus;
import com.app.old_riders_bk.booking.entity.LocationCategory;
import com.app.old_riders_bk.booking.entity.RideTierPreference;
import com.app.old_riders_bk.booking.entity.ScheduleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateBookingRequest {
        @NotNull(message = "Rider ID is required")
        private Long riderId;

        @NotBlank(message = "Pickup address is required")
        private String pickupAddress;
        private Double pickupLatitude;
        private Double pickupLongitude;

        @NotBlank(message = "Destination address is required")
        private String destinationAddress;
        private Double destinationLatitude;
        private Double destinationLongitude;

        @Builder.Default
        private LocationCategory destinationCategory = LocationCategory.OTHER;

        @NotNull(message = "Ride preference is required")
        private RideTierPreference ridePreference;

        @Builder.Default
        private ScheduleType scheduleType = ScheduleType.NOW;
        private LocalDateTime scheduledPickupTime;

        @NotNull(message = "Ride price is required")
        private BigDecimal ridePrice;

        private Double distanceKm;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RebookRideRequest {
        private ScheduleType scheduleType = ScheduleType.NOW;
        private LocalDateTime scheduledPickupTime;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcceptBookingRequest {
        @NotNull(message = "Driver ID is required")
        private Long driverId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeclineBookingRequest {
        @NotNull(message = "Driver ID is required")
        private Long driverId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartRideRequest {
        @NotNull(message = "Driver ID is required")
        private Long driverId;

        @NotBlank(message = "Pickup code is required")
        private String pickupCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteRideRequest {
        @NotNull(message = "Driver ID is required")
        private Long driverId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelBookingRequest {
        @NotNull(message = "Rider ID is required")
        private Long riderId;

        private String cancellationReason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingResponse {
        private Long bookingId;
        private Long riderId;
        private String riderFullName;
        private String riderPhoneNumber;
        private String pickupCode;

        private String pickupAddress;
        private Double pickupLatitude;
        private Double pickupLongitude;

        private String destinationAddress;
        private Double destinationLatitude;
        private Double destinationLongitude;
        private LocationCategory destinationCategory;

        private RideTierPreference ridePreference;
        private ScheduleType scheduleType;
        private LocalDateTime scheduledPickupTime;
        private BigDecimal ridePrice;
        private Double distanceKm;

        private Boolean isAccepted;
        private BookingStatus status;
        private DriverSummaryDto driver;

        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Boolean isPaidOut;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiderActivityItemResponse {
        private Long bookingId;
        private String pickupCode;

        private String pickupAddress;
        private Double pickupLatitude;
        private Double pickupLongitude;

        private String destinationAddress;
        private Double destinationLatitude;
        private Double destinationLongitude;

        private LocationCategory destinationCategory;
        private RideTierPreference ridePreference;
        private LocalDateTime rideDateTime;
        private BigDecimal ridePrice;
        private Double distanceKm;

        private BookingStatus status;
        private Boolean isAccepted;
        private DriverSummaryDto driver;

        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DriverSummaryDto {
        private Long driverId;
        private String fullName;
        private String phoneNumber;
        private String vehicleModel;
        private String registrationPlate;
        private Double rating;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiderActivityDashboardResponse {
        @Builder.Default
        private List<RiderActivityItemResponse> liveBookings = new ArrayList<>();

        @Builder.Default
        private List<RiderActivityItemResponse> thisWeek = new ArrayList<>();

        @Builder.Default
        private List<RiderActivityItemResponse> lastWeek = new ArrayList<>();

        @Builder.Default
        private List<RiderActivityItemResponse> pastMonths = new ArrayList<>();

        private int totalCompleted;
        private int totalCancelled;
        private int totalRides;
    }
}