package com.app.old_riders_bk.driver.controller;

import com.app.old_riders_bk.driver.dto.DriverDtos.*;
import com.app.old_riders_bk.driver.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    // ==========================================
    // --- 1. ONBOARDING & AUTH ---
    // ==========================================

    @PostMapping("/register")
    public ResponseEntity<DriverResponse> register(@Valid @RequestBody DriverRegisterRequest request) {
        return ResponseEntity.ok(driverService.registerDriver(request));
    }

    @PostMapping("/login")
    public ResponseEntity<DriverResponse> login(@Valid @RequestBody DriverLoginRequest request) {
        return ResponseEntity.ok(driverService.login(request));
    }

    // ==========================================
    // --- 2. DRIVER PROFILE CRUD ---
    // ==========================================

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResponse> getProfile(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverService.getDriverProfile(driverId));
    }

    @PutMapping("/{driverId}")
    public ResponseEntity<DriverResponse> updateProfile(
            @PathVariable Long driverId,
            @Valid @RequestBody UpdateDriverProfileRequest request) {
        return ResponseEntity.ok(driverService.updateProfile(driverId, request));
    }

    @PatchMapping("/{driverId}/documents")
    public ResponseEntity<DriverResponse> updateDocuments(
            @PathVariable Long driverId,
            @Valid @RequestBody UpdateDriverDocumentsRequest request) {
        return ResponseEntity.ok(driverService.updateDocuments(driverId, request));
    }

    @PatchMapping("/{driverId}/change-password")
    public ResponseEntity<String> changePassword(
            @PathVariable Long driverId,
            @Valid @RequestBody ChangeDriverPasswordRequest request) {
        driverService.changePassword(driverId, request);
        return ResponseEntity.ok("Password successfully updated.");
    }

    @DeleteMapping("/{driverId}")
    public ResponseEntity<String> deleteDriver(@PathVariable Long driverId) {
        driverService.deleteDriver(driverId);
        return ResponseEntity.ok("Driver account deactivated successfully.");
    }

    // ==========================================
    // --- 3. "MY VEHICLE" CRUD OPERATIONS ---
    // ==========================================

    // READ
    @GetMapping("/{driverId}/vehicle")
    public ResponseEntity<MyVehicleResponse> getMyVehicle(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverService.getMyVehicle(driverId));
    }

    // CREATE / FULL REPLACE (PUT)
    @PutMapping("/{driverId}/vehicle")
    public ResponseEntity<MyVehicleResponse> saveOrUpdateMyVehicle(
            @PathVariable Long driverId,
            @Valid @RequestBody SaveMyVehicleRequest request) {
        return ResponseEntity.ok(driverService.saveOrUpdateMyVehicle(driverId, request));
    }

    // PATCH: Vehicle Specs Only (Model, Color, Plate, Seats, Wheelchair)
    @PatchMapping("/{driverId}/vehicle/specs")
    public ResponseEntity<MyVehicleResponse> patchVehicleSpecs(
            @PathVariable Long driverId,
            @RequestBody PatchVehicleSpecsRequest request) {
        return ResponseEntity.ok(driverService.patchVehicleSpecs(driverId, request));
    }

    // PATCH: Features Only (Air Conditioning, Wheelchair Ramp, etc.)
    @PatchMapping("/{driverId}/vehicle/features")
    public ResponseEntity<MyVehicleResponse> updateVehicleFeatures(
            @PathVariable Long driverId,
            @Valid @RequestBody UpdateVehicleFeaturesRequest request) {
        return ResponseEntity.ok(driverService.updateVehicleFeatures(driverId, request));
    }

    // PATCH: Compliance Documents & Dates Only (Licence Disc, Roadworthy, Insurance)
    @PatchMapping("/{driverId}/vehicle/compliance")
    public ResponseEntity<MyVehicleResponse> updateVehicleCompliance(
            @PathVariable Long driverId,
            @RequestBody UpdateVehicleComplianceRequest request) {
        return ResponseEntity.ok(driverService.updateVehicleCompliance(driverId, request));
    }

    // DELETE: Remove Vehicle
    @DeleteMapping("/{driverId}/vehicle")
    public ResponseEntity<String> deleteVehicle(@PathVariable Long driverId) {
        driverService.deleteVehicle(driverId);
        return ResponseEntity.ok("Vehicle details removed successfully. Please register a vehicle to go online.");
    }

    // ==========================================
    // --- 4. STATUS & SHIFT TOGGLE ---
    // ==========================================

    @PatchMapping("/{driverId}/online-status")
    public ResponseEntity<DriverResponse> updateOnlineStatus(
            @PathVariable Long driverId,
            @RequestParam Boolean isOnline) {
        return ResponseEntity.ok(driverService.updateOnlineStatus(driverId, isOnline));
    }
}