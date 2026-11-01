package com.app.old_riders_bk.user.controller;

import com.app.old_riders_bk.user.dto.RiderProfileDtos.*;
import com.app.old_riders_bk.user.service.RiderProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/riders")
@RequiredArgsConstructor
public class RiderProfileController {

    private final RiderProfileService profileService;

    // 1. Get Profile
    @GetMapping("/{riderId}")
    public ResponseEntity<RiderProfileResponse> getProfile(@PathVariable Long riderId) {
        return ResponseEntity.ok(profileService.getProfile(riderId));
    }

    // 2. Full Update (Personal, Address, Emergency, Mobility, Payment)
    @PutMapping("/{riderId}")
    public ResponseEntity<RiderProfileResponse> updateProfile(
            @PathVariable Long riderId,
            @Valid @RequestBody UpdateRiderProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(riderId, request));
    }

    // 3. Quick toggle: Share ride with family
    @PatchMapping("/{riderId}/share-ride")
    public ResponseEntity<RiderProfileResponse> toggleShareRide(
            @PathVariable Long riderId,
            @Valid @RequestBody ToggleShareRideRequest request) {
        return ResponseEntity.ok(profileService.toggleShareRide(riderId, request.getShareRideWithFamily()));
    }

    // 4. Quick update: How we help you ride (Mobility needs)
    @PatchMapping("/{riderId}/mobility-needs")
    public ResponseEntity<RiderProfileResponse> updateMobilityNeeds(
            @PathVariable Long riderId,
            @Valid @RequestBody UpdateMobilityNeedsRequest request) {
        return ResponseEntity.ok(profileService.updateMobilityNeeds(riderId, request));
    }

    // 5. Quick update: Preferred Payment Method
    @PatchMapping("/{riderId}/payment-method")
    public ResponseEntity<RiderProfileResponse> updatePaymentMethod(
            @PathVariable Long riderId,
            @Valid @RequestBody UpdatePaymentMethodRequest request) {
        return ResponseEntity.ok(profileService.updatePaymentMethod(riderId, request));
    }

    // 6. Disable/Deactivate account
    @DeleteMapping("/{riderId}")
    public ResponseEntity<String> disableAccount(@PathVariable Long riderId) {
        profileService.deleteOrDisableAccount(riderId);
        return ResponseEntity.ok("Rider account successfully disabled.");
    }
}