package com.app.old_riders_bk.booking.service;

import com.app.old_riders_bk.booking.dto.BookingDtos.*;
import com.app.old_riders_bk.booking.entity.*;
import com.app.old_riders_bk.booking.repository.RideBookingRepository;
import com.app.old_riders_bk.common.PickupCodeUtil;
import com.app.old_riders_bk.driver.entity.Driver;
import com.app.old_riders_bk.driver.repository.DriverRepository;
import com.app.old_riders_bk.user.entity.Rider;
import com.app.old_riders_bk.user.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RideBookingService {

    private final RideBookingRepository bookingRepository;
    private final RiderRepository riderRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        Rider rider = riderRepository.findById(request.getRiderId())
                .orElseThrow(() -> new IllegalArgumentException("Rider not found with ID: " + request.getRiderId()));

        if (Boolean.TRUE.equals(rider.getIsDisabled())) {
            throw new IllegalStateException("Disabled accounts cannot book rides.");
        }

        if (request.getScheduleType() == ScheduleType.LATER && request.getScheduledPickupTime() == null) {
            throw new IllegalArgumentException("Scheduled pickup time is required when booking for 'LATER'.");
        }

        LocationPoint pickup = LocationPoint.builder()
                .addressText(request.getPickupAddress())
                .latitude(request.getPickupLatitude())
                .longitude(request.getPickupLongitude())
                .build();

        LocationPoint destination = LocationPoint.builder()
                .addressText(request.getDestinationAddress())
                .latitude(request.getDestinationLatitude())
                .longitude(request.getDestinationLongitude())
                .build();

        RideBooking booking = RideBooking.builder()
                .rider(rider)
                .pickupLocation(pickup)
                .destinationLocation(destination)
                .destinationCategory(request.getDestinationCategory() != null ? request.getDestinationCategory() : LocationCategory.OTHER)
                .ridePreference(request.getRidePreference())
                .scheduleType(request.getScheduleType())
                .scheduledPickupTime(request.getScheduleType() == ScheduleType.LATER ? request.getScheduledPickupTime() : LocalDateTime.now())
                .ridePrice(request.getRidePrice())
                .distanceKm(request.getDistanceKm())
                .isAccepted(false)
                .isPaidOut(false)
                .status(BookingStatus.REQUESTED)
                .build();

        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse rebookRide(Long previousBookingId, RebookRideRequest request) {
        RideBooking previousRide = bookingRepository.findById(previousBookingId)
                .orElseThrow(() -> new IllegalArgumentException("Original booking not found."));

        if (request.getScheduleType() == ScheduleType.LATER && request.getScheduledPickupTime() == null) {
            throw new IllegalArgumentException("Scheduled pickup time is required for future booking.");
        }

        RideBooking newBooking = RideBooking.builder()
                .rider(previousRide.getRider())
                .pickupLocation(previousRide.getPickupLocation())
                .destinationLocation(previousRide.getDestinationLocation())
                .destinationCategory(previousRide.getDestinationCategory())
                .ridePreference(previousRide.getRidePreference())
                .ridePrice(previousRide.getRidePrice())
                .distanceKm(previousRide.getDistanceKm())
                .scheduleType(request.getScheduleType())
                .scheduledPickupTime(request.getScheduleType() == ScheduleType.LATER ? request.getScheduledPickupTime() : LocalDateTime.now())
                .isAccepted(false)
                .isPaidOut(false)
                .status(BookingStatus.REQUESTED)
                .driver(null)
                .build();

        return mapToResponse(bookingRepository.save(newBooking));
    }

    @Transactional
    public BookingResponse acceptBooking(Long bookingId, Long driverId) {
        RideBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (Boolean.TRUE.equals(booking.getIsAccepted())) {
            throw new IllegalStateException("Ride has already been accepted by another driver.");
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + driverId));

        booking.setIsAccepted(true);
        booking.setDriver(driver);
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setPickupCode(PickupCodeUtil.generatePickupCode());

        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public void declineBooking(Long bookingId, Long driverId) {
        RideBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (!driverRepository.existsById(driverId)) {
            throw new IllegalArgumentException("Driver not found with ID: " + driverId);
        }

        booking.getDeclinedDriverIds().add(driverId);
        bookingRepository.save(booking);
    }

    @Transactional
    public BookingResponse startRide(Long bookingId, StartRideRequest request) {
        RideBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (booking.getDriver() == null || !booking.getDriver().getId().equals(request.getDriverId())) {
            throw new IllegalStateException("You are not the assigned driver for this ride.");
        }

        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new IllegalStateException("Ride cannot be started in status: " + booking.getStatus());
        }

        if (booking.getPickupCode() == null || !booking.getPickupCode().equalsIgnoreCase(request.getPickupCode().trim())) {
            throw new IllegalArgumentException("Invalid pickup verification code.");
        }

        booking.setStatus(BookingStatus.IN_PROGRESS);
        booking.setStartedAt(LocalDateTime.now());

        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse completeRide(Long bookingId, Long driverId) {
        RideBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (booking.getDriver() == null || !booking.getDriver().getId().equals(driverId)) {
            throw new IllegalStateException("You are not the assigned driver for this ride.");
        }

        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress rides can be marked as completed.");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());

        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse cancelBookingByRider(Long bookingId, CancelBookingRequest request) {
        RideBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        if (!booking.getRider().getId().equals(request.getRiderId())) {
            throw new IllegalStateException("You are not authorized to cancel this booking.");
        }

        if (booking.getStatus() == BookingStatus.IN_PROGRESS) {
            throw new IllegalStateException("The trip is already in progress and cannot be cancelled.");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Completed trips cannot be cancelled.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("This booking has already been cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setIsAccepted(false);

        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public RiderActivityDashboardResponse getRiderActivityDashboard(Long riderId, String statusFilter) {
        if (!riderRepository.existsById(riderId)) {
            throw new IllegalArgumentException("Rider not found with ID: " + riderId);
        }

        List<RideBooking> allBookings = bookingRepository.findByRiderIdOrderByCreatedAtDesc(riderId);

        // 1. Live/active bookings (REQUESTED, ACCEPTED, IN_PROGRESS)
        List<RiderActivityItemResponse> liveBookings = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.REQUESTED
                        || b.getStatus() == BookingStatus.ACCEPTED
                        || b.getStatus() == BookingStatus.IN_PROGRESS)
                .map(this::mapToActivityItem)
                .collect(Collectors.toList());

        // 2. Historical stream (COMPLETED or CANCELLED)
        List<RideBooking> historyStream = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED || b.getStatus() == BookingStatus.CANCELLED)
                .collect(Collectors.toList());

        int completedCount = (int) historyStream.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
        int cancelledCount = (int) historyStream.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();

        // 3. Optional filter: ALL, COMPLETED, CANCELLED
        if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
            historyStream = historyStream.stream()
                    .filter(b -> b.getStatus().name().equalsIgnoreCase(statusFilter.trim()))
                    .collect(Collectors.toList());
        }

        // 4. Date boundary splits
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfLastWeek = startOfThisWeek.minusWeeks(1);

        List<RiderActivityItemResponse> thisWeekList = new ArrayList<>();
        List<RiderActivityItemResponse> lastWeekList = new ArrayList<>();
        List<RiderActivityItemResponse> pastMonthsList = new ArrayList<>();

        for (RideBooking booking : historyStream) {
            LocalDateTime bookingDate = booking.getCreatedAt();

            if (!bookingDate.isBefore(startOfThisWeek)) {
                thisWeekList.add(mapToActivityItem(booking));
            } else if (!bookingDate.isBefore(startOfLastWeek) && bookingDate.isBefore(startOfThisWeek)) {
                lastWeekList.add(mapToActivityItem(booking));
            } else {
                pastMonthsList.add(mapToActivityItem(booking));
            }
        }

        return RiderActivityDashboardResponse.builder()
                .liveBookings(liveBookings)
                .thisWeek(thisWeekList)
                .lastWeek(lastWeekList)
                .pastMonths(pastMonthsList)
                .totalCompleted(completedCount)
                .totalCancelled(cancelledCount)
                .totalRides(allBookings.size())
                .build();
    }

    @Transactional(readOnly = true)
    public List<RiderActivityItemResponse> getRiderActivity(Long riderId) {
        return bookingRepository.findByRiderIdOrderByCreatedAtDesc(riderId)
                .stream()
                .map(this::mapToActivityItem)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getOpenRequests() {
        return bookingRepository.findByIsAcceptedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getOpenRequestsForDriver(Long driverId) {
        return bookingRepository.findAvailableRidesForDriver(BookingStatus.REQUESTED, driverId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RiderActivityItemResponse mapToActivityItem(RideBooking booking) {
        return RiderActivityItemResponse.builder()
                .bookingId(booking.getId())
                .pickupCode(booking.getPickupCode() != null ? booking.getPickupCode() : "N/A")
                .pickupAddress(booking.getPickupLocation() != null ? booking.getPickupLocation().getAddressText() : null)
                .pickupLatitude(booking.getPickupLocation() != null ? booking.getPickupLocation().getLatitude() : null)
                .pickupLongitude(booking.getPickupLocation() != null ? booking.getPickupLocation().getLongitude() : null)
                .destinationAddress(booking.getDestinationLocation() != null ? booking.getDestinationLocation().getAddressText() : null)
                .destinationLatitude(booking.getDestinationLocation() != null ? booking.getDestinationLocation().getLatitude() : null)
                .destinationLongitude(booking.getDestinationLocation() != null ? booking.getDestinationLocation().getLongitude() : null)
                .destinationCategory(booking.getDestinationCategory())
                .ridePreference(booking.getRidePreference())
                .rideDateTime(booking.getScheduledPickupTime() != null ? booking.getScheduledPickupTime() : booking.getCreatedAt())
                .ridePrice(booking.getRidePrice())
                .distanceKm(booking.getDistanceKm())
                .status(booking.getStatus())
                .isAccepted(booking.getIsAccepted())
                .driver(mapDriver(booking.getDriver()))
                .createdAt(booking.getCreatedAt())
                .startedAt(booking.getStartedAt())
                .completedAt(booking.getCompletedAt())
                .build();
    }

    private BookingResponse mapToResponse(RideBooking booking) {
        BookingResponse res = new BookingResponse();
        res.setBookingId(booking.getId());
        res.setRiderId(booking.getRider().getId());
        res.setRiderFullName(booking.getRider().getFullName());
        res.setRiderPhoneNumber(booking.getRider().getPhoneNumber());
        res.setPickupCode(booking.getPickupCode());

        if (booking.getPickupLocation() != null) {
            res.setPickupAddress(booking.getPickupLocation().getAddressText());
            res.setPickupLatitude(booking.getPickupLocation().getLatitude());
            res.setPickupLongitude(booking.getPickupLocation().getLongitude());
        }

        if (booking.getDestinationLocation() != null) {
            res.setDestinationAddress(booking.getDestinationLocation().getAddressText());
            res.setDestinationLatitude(booking.getDestinationLocation().getLatitude());
            res.setDestinationLongitude(booking.getDestinationLocation().getLongitude());
        }

        res.setDestinationCategory(booking.getDestinationCategory());
        res.setRidePreference(booking.getRidePreference());
        res.setScheduleType(booking.getScheduleType());
        res.setScheduledPickupTime(booking.getScheduledPickupTime());
        res.setRidePrice(booking.getRidePrice());
        res.setDistanceKm(booking.getDistanceKm());
        res.setIsAccepted(booking.getIsAccepted());
        res.setDriver(mapDriver(booking.getDriver()));
        res.setStatus(booking.getStatus());
        res.setStartedAt(booking.getStartedAt());
        res.setCompletedAt(booking.getCompletedAt());
        res.setIsPaidOut(booking.getIsPaidOut());
        res.setCreatedAt(booking.getCreatedAt());
        return res;
    }

    private DriverSummaryDto mapDriver(Driver driver) {
        if (driver == null) return null;
        return DriverSummaryDto.builder()
                .driverId(driver.getId())
                .fullName(driver.getFullName())
                .phoneNumber(driver.getPhoneNumber())
                .vehicleModel(driver.getVehicleModel())
                .registrationPlate(driver.getRegistrationPlate())
                .rating(driver.getRating())
                .build();
    }
}