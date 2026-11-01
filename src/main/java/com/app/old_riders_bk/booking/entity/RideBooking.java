package com.app.old_riders_bk.booking.entity;

import com.app.old_riders_bk.driver.entity.Driver;
import com.app.old_riders_bk.user.entity.Rider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ride_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private Rider rider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "addressText", column = @Column(name = "pickup_address", nullable = false)),
            @AttributeOverride(name = "latitude", column = @Column(name = "pickup_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "pickup_longitude"))
    })
    private LocationPoint pickupLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "addressText", column = @Column(name = "destination_address", nullable = false)),
            @AttributeOverride(name = "latitude", column = @Column(name = "destination_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "destination_longitude"))
    })
    private LocationPoint destinationLocation;

    // Place type of the rider's destination (Hospital, Church, Supermarket, etc.)
    @Enumerated(EnumType.STRING)
    @Column(name = "destination_category", nullable = false)
    @Builder.Default
    private LocationCategory destinationCategory = LocationCategory.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(name = "ride_preference", nullable = false)
    private RideTierPreference ridePreference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ScheduleType scheduleType = ScheduleType.NOW;

    private LocalDateTime scheduledPickupTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal ridePrice;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAccepted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.REQUESTED;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "booking_declined_drivers", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "driver_id")
    @Builder.Default
    private Set<Long> declinedDriverIds = new HashSet<>();

    // Estimated distance in kilometers (e.g. 5.4 km)
    private Double distanceKm;

    @Column(name = "pickup_code", length = 20)
    private String pickupCode;

    private LocalDateTime startedAt;

    // Under RideBooking entity
    private LocalDateTime completedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPaidOut = false; // false = ready to pay out
}