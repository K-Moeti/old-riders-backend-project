package com.app.old_riders_bk.payment.controller;

import com.app.old_riders_bk.booking.dto.BookingDtos.CreateBookingRequest;
import com.app.old_riders_bk.payment.service.PaystackPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/paystack")
@RequiredArgsConstructor
public class PaystackController {

    private final PaystackPaymentService paystackService;

    // 1. Start a card or EFT payment. Returns the checkout link. Nothing is booked yet.
    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody CreateBookingRequest request) {
        try {
            return ResponseEntity.ok(paystackService.startPayment(request));
        } catch (RuntimeException ex) {
            log.error("Could not start payment: {}", ex.getMessage(), ex);
            return failure(ex, "Online payment could not be started.");
        }
    }

    // 2. After the checkout is finished, confirm the payment and book the ride.
    @PostMapping("/complete")
    public ResponseEntity<?> complete(@RequestBody Map<String, String> body) {
        String reference = body.get("reference");
        if (reference == null || reference.isBlank()) {
            return ResponseEntity.badRequest().body(Map.<String, Object>of(
                    "status", false, "message", "Payment reference is missing."));
        }
        try {
            return ResponseEntity.ok(paystackService.completePayment(reference));
        } catch (RuntimeException ex) {
            log.error("Could not complete payment {}: {}", reference, ex.getMessage(), ex);
            return failure(ex, "The payment could not be completed.");
        }
    }

    // Paystack sends the rider here at the end of checkout. The app closes the
    // page before it loads, so this only shows if the page is opened in a browser.
    @GetMapping("/callback")
    public ResponseEntity<String> callback() {
        return ResponseEntity.ok("Payment step finished. You can return to the app.");
    }

    private ResponseEntity<?> failure(RuntimeException ex, String fallback) {
        String message = ex.getMessage() != null ? ex.getMessage() : fallback;
        return ResponseEntity.badRequest().body(Map.<String, Object>of("status", false, "message", message));
    }
}