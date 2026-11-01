package com.app.old_riders_bk.admin.controller;

import com.app.old_riders_bk.admin.dto.AdminDtos.*;
import com.app.old_riders_bk.admin.service.AdminService;
import com.app.old_riders_bk.driver.entity.DriverStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 1. Admin Login
    @PostMapping("/login")
    public ResponseEntity<AdminResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminService.login(request));
    }

    // 2. Super Admin creates another Admin
    @PostMapping("/create")
    public ResponseEntity<AdminResponse> createAdmin(
            @RequestParam Long requestingAdminId,
            @Valid @RequestBody CreateAdminRequest request) {
        return ResponseEntity.ok(adminService.createAdmin(requestingAdminId, request));
    }

    // 3. List drivers by status (e.g. ?status=PENDING_APPROVAL)
    @GetMapping("/drivers")
    public ResponseEntity<List<DriverDocumentReviewCard>> getDrivers(
            @RequestParam(required = false) DriverStatus status) {
        return ResponseEntity.ok(adminService.getDriversByStatus(status));
    }

    // 4. Inspect specific driver's documents
    @GetMapping("/drivers/{driverId}")
    public ResponseEntity<DriverDocumentReviewCard> getDriverDetails(@PathVariable Long driverId) {
        return ResponseEntity.ok(adminService.getDriverDetailsForReview(driverId));
    }

    // 5. Approve or Reject Driver
    @PutMapping("/drivers/{driverId}/review")
    public ResponseEntity<DriverDocumentReviewCard> reviewDriver(
            @PathVariable Long driverId,
            @Valid @RequestBody ReviewDriverRequest request) {
        return ResponseEntity.ok(adminService.reviewDriverDocuments(driverId, request));
    }

    // 6. Super Admin Overview Analytics
    @GetMapping("/analytics")
    public ResponseEntity<SuperAdminDashboardStats> getPlatformStats(@RequestParam Long adminId) {
        return ResponseEntity.ok(adminService.getPlatformStats(adminId));
    }
}