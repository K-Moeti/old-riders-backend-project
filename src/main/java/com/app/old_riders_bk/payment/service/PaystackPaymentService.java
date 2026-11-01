package com.app.old_riders_bk.payment.service;

import com.app.old_riders_bk.booking.dto.BookingDtos.BookingResponse;
import com.app.old_riders_bk.booking.dto.BookingDtos.CreateBookingRequest;
import com.app.old_riders_bk.booking.service.RideBookingService;
import com.app.old_riders_bk.payment.dto.PaystackDtos.InitializePaymentRequest;
import com.app.old_riders_bk.payment.dto.PaystackDtos.InitializePaymentResponse;
import com.app.old_riders_bk.payment.dto.PaystackDtos.VerifyPaymentResponse;
import com.app.old_riders_bk.payment.entity.PaymentRecord;
import com.app.old_riders_bk.payment.repository.PaymentRecordRepository;
import com.app.old_riders_bk.user.entity.Rider;
import com.app.old_riders_bk.user.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaystackPaymentService {

    private static final Pattern PAYSTACK_MESSAGE = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]*)\"");

    private final PaymentRecordRepository paymentRecords;
    private final RiderRepository riderRepository;
    private final RideBookingService rideBookingService;
    private final WebClient.Builder webClientBuilder;

    @Value("${paystack.secret.key}")
    private String secretKey;

    @Value("${paystack.initialize.url}")
    private String initializeUrl;

    @Value("${paystack.verify.url}")
    private String verifyUrl;

    @Value("${paystack.callback.url}")
    private String callbackUrl;

    /**
     * Step 1. Starts a card or EFT payment for a ride that is not booked yet.
     * Returns the Paystack checkout link. Nothing is booked here.
     */
    @Transactional
    public Map<String, Object> startPayment(CreateBookingRequest request) {
        if (request.getRiderId() == null) {
            throw new IllegalArgumentException("Rider ID is required.");
        }
        Rider rider = riderRepository.findById(request.getRiderId())
                .orElseThrow(() -> new IllegalArgumentException("Rider not found with ID: " + request.getRiderId()));

        if (Boolean.TRUE.equals(rider.getIsDisabled())) {
            throw new IllegalStateException("Disabled accounts cannot book rides.");
        }
        if (request.getRidePrice() == null || request.getRidePrice().signum() <= 0) {
            throw new IllegalStateException("The ride needs a price before it can be paid online.");
        }

        // Convert ZAR decimal to cents (e.g., 120.50 -> 12050)
        long amountCents = request.getRidePrice()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        String reference = "ORD-" + rider.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);

        // Keep the ride details until the payment is confirmed.
        PaymentRecord record = new PaymentRecord();
        record.setReference(reference);
        record.setRiderId(rider.getId());
        record.setAmountCents(amountCents);
        record.setPickupAddress(request.getPickupAddress());
        record.setPickupLatitude(request.getPickupLatitude());
        record.setPickupLongitude(request.getPickupLongitude());
        record.setDestinationAddress(request.getDestinationAddress());
        record.setDestinationLatitude(request.getDestinationLatitude());
        record.setDestinationLongitude(request.getDestinationLongitude());
        record.setDestinationCategory(request.getDestinationCategory());
        record.setRidePreference(request.getRidePreference());
        record.setScheduleType(request.getScheduleType());
        record.setScheduledPickupTime(request.getScheduledPickupTime());
        record.setRidePrice(request.getRidePrice());
        record.setDistanceKm(request.getDistanceKm());
        paymentRecords.save(record);

        String name = rider.getFullName() != null ? rider.getFullName().trim() : "";
        String phone = rider.getPhoneNumber() != null ? rider.getPhoneNumber().trim() : "";
        String digits = phone.replaceAll("[^0-9]", "");
        String email = (digits.isEmpty() ? "rider" + rider.getId() : digits) + "@oldriders.co.za";

        // Paystack metadata cannot hold nulls, so every value is a string.
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("riderId", String.valueOf(rider.getId()));
        metadata.put("riderName", name);
        metadata.put("riderPhone", phone);

        InitializePaymentRequest payload = InitializePaymentRequest.builder()
                .email(email)
                .amount(amountCents)
                .currency("ZAR")
                .reference(reference)
                .callbackUrl(callbackUrl)
                .metadata(metadata)
                .build();

        log.info("Starting Paystack payment {} for rider {}", reference, rider.getId());
        InitializePaymentResponse response = initialize(payload);

        Map<String, Object> result = new HashMap<>();
        result.put("status", true);
        result.put("reference", reference);
        result.put("authorizationUrl", response.getData().getAuthorizationUrl());
        return result;
    }

    /**
     * Step 2. Called when the rider finishes the checkout. Confirms the payment
     * with Paystack, then books the ride.
     */
    @Transactional
    public BookingResponse completePayment(String reference) {
        PaymentRecord record = paymentRecords.findByReference(reference)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found."));

        if (!"PENDING".equals(record.getStatus())) {
            throw new IllegalStateException("This payment has already been used for a booking.");
        }

        VerifyPaymentResponse verified = verify(reference);
        if (verified == null || !verified.isStatus() || verified.getData() == null
                || !"success".equalsIgnoreCase(verified.getData().getStatus())) {
            throw new IllegalStateException("The payment was not successful. Nothing was booked.");
        }

        Long paidCents = verified.getData().getAmount();
        if (paidCents == null || paidCents.longValue() != record.getAmountCents().longValue()) {
            log.error("Amount mismatch for {}: paid {}, expected {}", reference, paidCents, record.getAmountCents());
            throw new IllegalStateException("The amount paid does not match the ride price.");
        }

        // Rebuild the booking request from the stored ride details.
        CreateBookingRequest request = new CreateBookingRequest();
        request.setRiderId(record.getRiderId());
        request.setPickupAddress(record.getPickupAddress());
        request.setPickupLatitude(record.getPickupLatitude());
        request.setPickupLongitude(record.getPickupLongitude());
        request.setDestinationAddress(record.getDestinationAddress());
        request.setDestinationLatitude(record.getDestinationLatitude());
        request.setDestinationLongitude(record.getDestinationLongitude());
        request.setDestinationCategory(record.getDestinationCategory());
        request.setRidePreference(record.getRidePreference());
        request.setScheduleType(record.getScheduleType());
        request.setScheduledPickupTime(record.getScheduledPickupTime());
        request.setRidePrice(record.getRidePrice());
        request.setDistanceKm(record.getDistanceKm());

        BookingResponse booking = rideBookingService.createBooking(request);
        record.setStatus("PAID");
        record.setBookingId(booking.getBookingId());
        paymentRecords.save(record);
        log.info("Payment {} paid, booking {} created", reference, booking.getBookingId());
        return booking;
    }

    private InitializePaymentResponse initialize(InitializePaymentRequest payload) {
        try {
            InitializePaymentResponse response = webClientBuilder.build().post()
                    .uri(initializeUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(InitializePaymentResponse.class)
                    .block();
            if (response == null || !response.isStatus() || response.getData() == null
                    || response.getData().getAuthorizationUrl() == null) {
                String reason = response != null && response.getMessage() != null
                        ? response.getMessage()
                        : "Paystack did not return a payment link.";
                throw new IllegalStateException("Paystack: " + reason);
            }
            return response;
        } catch (WebClientResponseException ex) {
            String body = ex.getResponseBodyAsString();
            log.error("Paystack initialize failed {}: {}", ex.getStatusCode(), body);
            Matcher m = PAYSTACK_MESSAGE.matcher(body);
            String reason = m.find() ? m.group(1) : "Paystack rejected the payment request.";
            throw new IllegalStateException("Paystack: " + reason);
        }
    }

    private VerifyPaymentResponse verify(String reference) {
        try {
            return webClientBuilder.build().get()
                    .uri(verifyUrl + reference)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey.trim())
                    .retrieve()
                    .bodyToMono(VerifyPaymentResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Paystack verify failed {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new IllegalStateException("Could not confirm the payment with Paystack. Please try again.");
        }
    }
}