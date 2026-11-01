package com.app.old_riders_bk.driver.controller;

import com.app.old_riders_bk.driver.dto.DriverEarningsDtos.DriverEarningsDashboardResponse;
import com.app.old_riders_bk.driver.service.DriverEarningsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/driver/earnings")
@RequiredArgsConstructor
public class DriverEarningsController {

    private final DriverEarningsService earningsService;

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverEarningsDashboardResponse> getDriverEarnings(@PathVariable Long driverId) {
        return ResponseEntity.ok(earningsService.getEarningsDashboard(driverId));
    }
}