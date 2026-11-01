package com.app.old_riders_bk.payment.entity;

import com.app.old_riders_bk.booking.entity.LocationCategory;
import com.app.old_riders_bk.booking.entity.RideTierPreference;
import com.app.old_riders_bk.booking.entity.ScheduleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A card or EFT payment that is waiting for the booking to be made. It holds
 * the ride details, so the ride can be booked once Paystack confirms the payment.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payment_records")
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    /** PENDING until the ride is booked, then PAID. */
    @Column(nullable = false)
    private String status = "PENDING";

    /** Set once the ride is booked. */
    private Long bookingId;

    @Column(nullable = false)
    private Long riderId;

    @Column(nullable = false)
    private Long amountCents;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ───── ride details, kept until the payment is confirmed ─────

    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;

    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;

    @Enumerated(EnumType.STRING)
    private LocationCategory destinationCategory;

    @Enumerated(EnumType.STRING)
    private RideTierPreference ridePreference;

    @Enumerated(EnumType.STRING)
    private ScheduleType scheduleType;

    private LocalDateTime scheduledPickupTime;

    private BigDecimal ridePrice;

    private Double distanceKm;
}