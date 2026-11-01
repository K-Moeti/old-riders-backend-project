package com.app.old_riders_bk.driver.service;

import com.app.old_riders_bk.booking.entity.BookingStatus;
import com.app.old_riders_bk.booking.entity.RideBooking;
import com.app.old_riders_bk.booking.repository.RideBookingRepository;
import com.app.old_riders_bk.common.DistanceUtil;
import com.app.old_riders_bk.driver.dto.DriverEarningsDtos.*;
import com.app.old_riders_bk.driver.entity.DriverShift;
import com.app.old_riders_bk.driver.repository.DriverShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverEarningsService {

    private final RideBookingRepository bookingRepository;
    private final DriverShiftRepository shiftRepository;

    @Transactional(readOnly = true)
    public DriverEarningsDashboardResponse getEarningsDashboard(Long driverId) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Time Boundaries
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();

        // 2. Query completed rides
        List<RideBooking> todayRides = bookingRepository.findByDriverIdAndStatusAndUpdatedAtGreaterThanEqual(driverId, BookingStatus.COMPLETED, startOfToday);
        List<RideBooking> weekRides = bookingRepository.findByDriverIdAndStatusAndUpdatedAtGreaterThanEqual(driverId, BookingStatus.COMPLETED, startOfWeek);
        List<RideBooking> monthRides = bookingRepository.findByDriverIdAndStatusAndUpdatedAtGreaterThanEqual(driverId, BookingStatus.COMPLETED, startOfMonth);

        // 3. Ready to pay out (All completed rides where isPaidOut == false)
        List<RideBooking> unpaidRides = bookingRepository.findByDriverIdAndStatusAndIsPaidOutFalse(driverId, BookingStatus.COMPLETED);
        BigDecimal readyToPayout = unpaidRides.stream()
                .map(RideBooking::getRidePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Summaries
        PeriodSummary todaySummary = buildPeriodSummary(todayRides, calculateOnlineHours(driverId, startOfToday));
        PeriodSummary weekSummary = buildPeriodSummary(weekRides, calculateOnlineHours(driverId, startOfWeek));
        PeriodSummary monthSummary = buildPeriodSummary(monthRides, calculateOnlineHours(driverId, startOfMonth));

        // 5. Recent 10 payments
        List<RecentPaymentItem> recentPayments = bookingRepository.findTop10ByDriverIdAndStatusOrderByUpdatedAtDesc(driverId, BookingStatus.COMPLETED)
                .stream()
                .map(this::mapToPaymentItem)
                .collect(Collectors.toList());

        return DriverEarningsDashboardResponse.builder()
                .driverId(driverId)
                .readyToPayout(readyToPayout)
                .today(todaySummary)
                .thisWeek(weekSummary)
                .thisMonth(monthSummary)
                .recentPayments(recentPayments)
                .build();
    }

    private PeriodSummary buildPeriodSummary(List<RideBooking> rides, double onlineHours) {
        BigDecimal totalEarned = rides.stream()
                .map(RideBooking::getRidePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double totalDistance = rides.stream()
                .mapToDouble(r -> {
                    if (r.getDistanceKm() != null) return r.getDistanceKm();
                    if (r.getPickupLocation() != null && r.getDestinationLocation() != null) {
                        Double calc = DistanceUtil.calculateDistanceKm(
                                r.getPickupLocation().getLatitude(),
                                r.getPickupLocation().getLongitude(),
                                r.getDestinationLocation().getLatitude(),
                                r.getDestinationLocation().getLongitude()
                        );
                        return calc != null ? calc : 0.0;
                    }
                    return 0.0;
                })
                .sum();

        // Round distance to 1 decimal
        double roundedDistance = Math.round(totalDistance * 10.0) / 10.0;

        return PeriodSummary.builder()
                .totalEarned(totalEarned.setScale(2, RoundingMode.HALF_UP))
                .totalRides(rides.size())
                .totalDistanceKm(roundedDistance)
                .onlineHours(Math.round(onlineHours * 10.0) / 10.0)
                .build();
    }

    private double calculateOnlineHours(Long driverId, LocalDateTime fromTime) {
        List<DriverShift> shifts = shiftRepository.findByDriverIdAndStartedAtGreaterThanEqual(driverId, fromTime);
        long totalSeconds = 0;
        LocalDateTime now = LocalDateTime.now();

        for (DriverShift s : shifts) {
            LocalDateTime end = s.getEndedAt() != null ? s.getEndedAt() : now;
            LocalDateTime start = s.getStartedAt().isBefore(fromTime) ? fromTime : s.getStartedAt();
            totalSeconds += Duration.between(start, end).getSeconds();
        }

        return totalSeconds / 3600.0;
    }

    private RecentPaymentItem mapToPaymentItem(RideBooking r) {
        return RecentPaymentItem.builder()
                .bookingId(r.getId())
                .riderName(r.getRider() != null ? r.getRider().getFullName() : "Unknown Rider")
                .destinationAddress(r.getDestinationLocation() != null ? r.getDestinationLocation().getAddressText() : "N/A")
                .destinationCategory(r.getDestinationCategory())
                .ridePrice(r.getRidePrice())
                .paidAt(r.getUpdatedAt())
                .build();
    }
}