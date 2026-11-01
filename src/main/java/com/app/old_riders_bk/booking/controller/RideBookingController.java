package com.app.old_riders_bk.booking.controller;

import com.app.old_riders_bk.booking.dto.BookingDtos.*;
import com.app.old_riders_bk.booking.service.RideBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class RideBookingController {

    private final RideBookingService bookingService;

    // 1. Create a ride
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    // 2. Rider Activity (History with location category, driver details, price, status)
    @GetMapping("/rider/{riderId}/activity")
    public ResponseEntity<List<RiderActivityItemResponse>> getRiderActivity(@PathVariable Long riderId) {
        return ResponseEntity.ok(bookingService.getRiderActivity(riderId));
    }

    // 3. "Book Again" (Rebook from a past ride)
    @PostMapping("/{bookingId}/book-again")
    public ResponseEntity<BookingResponse> rebookRide(
            @PathVariable Long bookingId,
            @Valid @RequestBody RebookRideRequest request) {
        return ResponseEntity.ok(bookingService.rebookRide(bookingId, request));
    }

    // 4. Driver accepts a booking
    @PutMapping("/{bookingId}/accept")
    public ResponseEntity<BookingResponse> acceptBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody AcceptBookingRequest request) {
        return ResponseEntity.ok(bookingService.acceptBooking(bookingId, request.getDriverId()));
    }

    // 5. Driver declines a booking (excludes it from driver's open feed)
    @PutMapping("/{bookingId}/decline")
    public ResponseEntity<Void> declineBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody DeclineBookingRequest request) {
        bookingService.declineBooking(bookingId, request.getDriverId());
        return ResponseEntity.noContent().build();
    }

    // 6. Driver enters pickup code to start ride
    @PutMapping("/{bookingId}/start")
    public ResponseEntity<BookingResponse> startRide(
            @PathVariable Long bookingId,
            @Valid @RequestBody StartRideRequest request) {
        return ResponseEntity.ok(bookingService.startRide(bookingId, request));
    }

    // 7. Driver completes ride
    @PutMapping("/{bookingId}/complete")
    public ResponseEntity<BookingResponse> completeRide(
            @PathVariable Long bookingId,
            @Valid @RequestBody CompleteRideRequest request) {
        return ResponseEntity.ok(bookingService.completeRide(bookingId, request.getDriverId()));
    }

    // 8. Open requests for drivers (general)
    @GetMapping("/open-requests")
    public ResponseEntity<List<BookingResponse>> getOpenRequests() {
        return ResponseEntity.ok(bookingService.getOpenRequests());
    }

    // 9. Open requests filtered for a specific driver (excludes declined)
    @GetMapping("/open-requests/driver/{driverId}")
    public ResponseEntity<List<BookingResponse>> getOpenRequestsForDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(bookingService.getOpenRequestsForDriver(driverId));
    }

    // 10. Rider cancels a booking (works for both REQUESTED and ACCEPTED states)
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody CancelBookingRequest request) {
        return ResponseEntity.ok(bookingService.cancelBookingByRider(bookingId, request));
    }

    @GetMapping("/rider/{riderId}/activity-dashboard")
    public ResponseEntity<RiderActivityDashboardResponse> getRiderActivityDashboard(
            @PathVariable Long riderId,
            @RequestParam(value = "filter", defaultValue = "ALL") String filter) {
        return ResponseEntity.ok(bookingService.getRiderActivityDashboard(riderId, filter));
    }
}