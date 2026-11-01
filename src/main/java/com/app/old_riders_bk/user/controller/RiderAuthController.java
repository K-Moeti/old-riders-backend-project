package com.app.old_riders_bk.user.controller;

import com.app.old_riders_bk.user.dto.RiderDtos.*;
import com.app.old_riders_bk.user.dto.RiderProfileDtos.RiderProfileResponse;
import com.app.old_riders_bk.user.service.RiderAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RiderAuthController {

    private final RiderAuthService riderAuthService;

    // 1. Register rider profile
    @PostMapping("/register")
    public ResponseEntity<RiderProfileResponse> register(@Valid @RequestBody RiderRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(riderAuthService.registerRider(request));
    }

    // 2. Request OTP to login
    @PostMapping("/login-otp")
    public ResponseEntity<OtpResponse> requestLoginOtp(@Valid @RequestBody RequestLoginOtpRequest request) {
        return ResponseEntity.ok(riderAuthService.sendLoginOtp(request));
    }

    // 3. Resend OTP (subject to the 60-second cooldown timer)
    @PostMapping("/resend-otp")
    public ResponseEntity<OtpResponse> resendLoginOtp(@Valid @RequestBody ResendOtpRequest request) {
        return ResponseEntity.ok(riderAuthService.resendLoginOtp(request));
    }

    // 4. Verify OTP to finish login
    @PostMapping("/verify-otp")
    public ResponseEntity<RiderProfileResponse> verifyLoginOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(riderAuthService.verifyLoginOtp(request));
    }
}