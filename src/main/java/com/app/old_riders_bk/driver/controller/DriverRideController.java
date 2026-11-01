package com.app.old_riders_bk.driver.controller;

import com.app.old_riders_bk.booking.dto.DriverRideDtos.*;
import com.app.old_riders_bk.driver.service.DriverRideService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/driver/rides")
@RequiredArgsConstructor
public class DriverRideController {

    private final DriverRideService driverRideService;

    // 1. All 3 tabs in one call (New, Upcoming, Done)
    @GetMapping("/{driverId}/dashboard")
    public ResponseEntity<DriverDashboardRidesResponse> getDashboard(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverRideService.getDriverDashboard(driverId));
    }

    // 2. Individual Tab: NEW
    @GetMapping("/{driverId}/new")
    public ResponseEntity<List<DriverRideCardResponse>> getNewRides(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverRideService.getNewRides(driverId));
    }

    // 3. Individual Tab: UPCOMING
    @GetMapping("/{driverId}/upcoming")
    public ResponseEntity<List<DriverRideCardResponse>> getUpcomingRides(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverRideService.getUpcomingRides(driverId));
    }

    // 4. Individual Tab: DONE
    @GetMapping("/{driverId}/done")
    public ResponseEntity<List<DriverRideCardResponse>> getDoneRides(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverRideService.getDoneRides(driverId));
    }

    // 5. Action: ACCEPT a ride
    @PutMapping("/{bookingId}/accept")
    public ResponseEntity<DriverRideCardResponse> acceptRide(
            @PathVariable Long bookingId,
            @RequestParam Long driverId) {
        return ResponseEntity.ok(driverRideService.acceptRide(bookingId, driverId));
    }

    // 6. Action: DECLINE a ride
    @PutMapping("/{bookingId}/decline")
    public ResponseEntity<String> declineRide(
            @PathVariable Long bookingId,
            @RequestParam Long driverId) {
        driverRideService.declineRide(bookingId, driverId);
        return ResponseEntity.ok("Ride declined.");
    }

    // 7. Action: COMPLETE a ride (moves to DONE)
    @PutMapping("/{bookingId}/complete")
    public ResponseEntity<DriverRideCardResponse> completeRide(
            @PathVariable Long bookingId,
            @RequestParam Long driverId) {
        return ResponseEntity.ok(driverRideService.completeRide(bookingId, driverId));
    }

    // Driver Action: Verify pickup via scanned QR or manual code
    @PutMapping("/{bookingId}/verify-pickup")
    public ResponseEntity<DriverRideCardResponse> verifyPickup(
            @PathVariable Long bookingId,
            @RequestParam Long driverId,
            @RequestParam String pickupCode) {
        return ResponseEntity.ok(driverRideService.verifyPickupAndStartRide(bookingId, driverId, pickupCode));
    }
}