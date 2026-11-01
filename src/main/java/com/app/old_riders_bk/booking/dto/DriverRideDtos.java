package com.app.old_riders_bk.booking.dto;

import com.app.old_riders_bk.booking.entity.BookingStatus;
import com.app.old_riders_bk.booking.entity.LocationCategory;
import com.app.old_riders_bk.booking.entity.RideTierPreference;
import com.app.old_riders_bk.booking.entity.ScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DriverRideDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverRideCardResponse {
        private Long bookingId;
        private Long riderId;
        private String riderName;
        private String riderPhone;
        private LocalDateTime rideDateTime; // When the ride is for
        private String pickupAddress;
        private Double pickupLatitude;
        private Double pickupLongitude;
        private String dropOffAddress;
        private Double dropOffLatitude;
        private Double dropOffLongitude;
        private LocationCategory destinationCategory;
        private Double distanceKm;           // Calculated distance
        private RideTierPreference ridePreference; // COMFORT, WHEELCHAIR_FRIENDLY, etc.
        private BigDecimal ridePrice;
        private ScheduleType scheduleType;
        private BookingStatus status;
        private Boolean isAccepted;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverDashboardRidesResponse {
        private List<DriverRideCardResponse> newRides;      // Pending requests
        private List<DriverRideCardResponse> upcomingRides; // Accepted future/today rides
        private List<DriverRideCardResponse> doneRides;     // Completed trips
    }
}