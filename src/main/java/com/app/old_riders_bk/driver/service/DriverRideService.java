package com.app.old_riders_bk.driver.service;

import com.app.old_riders_bk.booking.dto.DriverRideDtos.*;
import com.app.old_riders_bk.booking.entity.BookingStatus;
import com.app.old_riders_bk.booking.entity.RideBooking;
import com.app.old_riders_bk.booking.repository.RideBookingRepository;
import com.app.old_riders_bk.common.DistanceUtil;
import com.app.old_riders_bk.common.PickupCodeUtil;
import com.app.old_riders_bk.driver.entity.Driver;
import com.app.old_riders_bk.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverRideService {

    private final RideBookingRepository bookingRepository;
    private final DriverRepository driverRepository;

    // Get all 3 tabs at once for the driver app
    @Transactional(readOnly = true)
    public DriverDashboardRidesResponse getDriverDashboard(Long driverId) {
        return DriverDashboardRidesResponse.builder()
                .newRides(getNewRides(driverId))
                .upcomingRides(getUpcomingRides(driverId))
                .doneRides(getDoneRides(driverId))
                .build();
    }

    // TAB 1: NEW (Requests waiting for driver, excluding those this driver declined)
    @Transactional(readOnly = true)
    public List<DriverRideCardResponse> getNewRides(Long driverId) {
        return bookingRepository.findByStatusAndIsAcceptedFalseOrderByCreatedAtDesc(BookingStatus.REQUESTED)
                .stream()
                .filter(b -> !b.getDeclinedDriverIds().contains(driverId))
                .map(this::mapToCard)
                .collect(Collectors.toList());
    }

    // TAB 2: UPCOMING (Accepted or In-Progress rides assigned to this driver)
    @Transactional(readOnly = true)
    public List<DriverRideCardResponse> getUpcomingRides(Long driverId) {
        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.ACCEPTED, BookingStatus.IN_PROGRESS);
        return bookingRepository.findByDriverIdAndStatusInOrderByScheduledPickupTimeAsc(driverId, activeStatuses)
                .stream()
                .map(this::mapToCard)
                .collect(Collectors.toList());
    }

    // TAB 3: DONE (Completed rides)
    @Transactional(readOnly = true)
    public List<DriverRideCardResponse> getDoneRides(Long driverId) {
        return bookingRepository.findByDriverIdAndStatusOrderByUpdatedAtDesc(driverId, BookingStatus.COMPLETED)
                .stream()
                .map(this::mapToCard)
                .collect(Collectors.toList());
    }

    // Driver Action: ACCEPT (generates the ORD-8digit-2Letters code)
    @Transactional
    public DriverRideCardResponse acceptRide(Long bookingId, Long driverId) {
        RideBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (Boolean.TRUE.equals(booking.getIsAccepted())) {
            throw new IllegalStateException("Ride has already been accepted by another driver.");
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found."));

        booking.setDriver(driver);
        booking.setIsAccepted(true);
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setPickupCode(PickupCodeUtil.generatePickupCode());

        return mapToCard(bookingRepository.save(booking));
    }

    // Driver Action: SCAN QR OR ENTER CODE TO START RIDE
    @Transactional
    public DriverRideCardResponse verifyPickupAndStartRide(Long bookingId, Long driverId, String enteredCode) {
        RideBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (booking.getDriver() == null || !booking.getDriver().getId().equals(driverId)) {
            throw new IllegalStateException("You are not the designated driver for this ride.");
        }

        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new IllegalStateException("Ride cannot be started. Current status: " + booking.getStatus());
        }

        // Clean up input (trim whitespace and ignore casing)
        String cleanedEnteredCode = enteredCode.trim().toUpperCase();
        if (!booking.getPickupCode().equalsIgnoreCase(cleanedEnteredCode)) {
            throw new IllegalArgumentException("Invalid pickup code. Verification failed.");
        }

        // Successfully verified: start the trip
        booking.setStatus(BookingStatus.IN_PROGRESS);
        booking.setStartedAt(LocalDateTime.now());

        return mapToCard(bookingRepository.save(booking));
    }

    // Driver Action: DECLINE
    @Transactional
    public void declineRide(Long bookingId, Long driverId) {
        RideBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        // Add this driver to the declined set so it won't show in their NEW list
        booking.getDeclinedDriverIds().add(driverId);
        bookingRepository.save(booking);
    }

    // Driver Action: Complete Ride (moves to DONE)
    @Transactional
    public DriverRideCardResponse completeRide(Long bookingId, Long driverId) {
        RideBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (!booking.getDriver().getId().equals(driverId)) {
            throw new IllegalStateException("You are not the assigned driver for this ride.");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        return mapToCard(bookingRepository.save(booking));
    }

    private DriverRideCardResponse mapToCard(RideBooking b) {
        Double distance = b.getDistanceKm();
        if (distance == null && b.getPickupLocation() != null && b.getDestinationLocation() != null) {
            distance = DistanceUtil.calculateDistanceKm(
                    b.getPickupLocation().getLatitude(),
                    b.getPickupLocation().getLongitude(),
                    b.getDestinationLocation().getLatitude(),
                    b.getDestinationLocation().getLongitude()
            );
        }

        return DriverRideCardResponse.builder()
                .bookingId(b.getId())
                .riderId(b.getRider().getId())
                .riderName(b.getRider().getFullName())
                .riderPhone(b.getRider().getPhoneNumber())
                .rideDateTime(b.getScheduledPickupTime() != null ? b.getScheduledPickupTime() : b.getCreatedAt())
                .pickupAddress(b.getPickupLocation().getAddressText())
                .pickupLatitude(b.getPickupLocation().getLatitude())
                .pickupLongitude(b.getPickupLocation().getLongitude())
                .dropOffAddress(b.getDestinationLocation().getAddressText())
                .dropOffLatitude(b.getDestinationLocation().getLatitude())
                .dropOffLongitude(b.getDestinationLocation().getLongitude())
                .destinationCategory(b.getDestinationCategory())
                .distanceKm(distance)
                .ridePreference(b.getRidePreference())
                .ridePrice(b.getRidePrice())
                .scheduleType(b.getScheduleType())
                .status(b.getStatus())
                .isAccepted(b.getIsAccepted())
                .build();
    }
}