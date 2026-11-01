package com.app.old_riders_bk.driver.dto;

import com.app.old_riders_bk.booking.entity.LocationCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DriverEarningsDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentPaymentItem {
        private Long bookingId;
        private String riderName;
        private String destinationAddress;
        private LocationCategory destinationCategory;
        private BigDecimal ridePrice;
        private LocalDateTime paidAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodSummary {
        private BigDecimal totalEarned;
        private Integer totalRides;
        private Double totalDistanceKm;
        private Double onlineHours; // e.g., 4.5 hours
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverEarningsDashboardResponse {
        private Long driverId;
        private BigDecimal readyToPayout; // Total accumulated unpaid money
        private PeriodSummary today;
        private PeriodSummary thisWeek;
        private PeriodSummary thisMonth;
        private List<RecentPaymentItem> recentPayments;
    }
}