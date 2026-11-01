package com.app.old_riders_bk.driver.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Embedded
    private DriverAddress address;

    @Embedded
    private DriverDocuments documents;

    @Column(nullable = false)
    private Boolean agreedToTerms;

    // --- Vehicle Specifications ---
    @Column(nullable = false)
    private String vehicleModel;

    @Column(nullable = false)
    private String vehicleColor;

    @Column(nullable = false)
    private String registrationPlate;

    @Column(nullable = false)
    private Integer seatingCapacity;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isWheelchairFriendly = false;

    @ElementCollection(targetClass = VehicleFeature.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "driver_vehicle_features", joinColumns = @JoinColumn(name = "driver_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "feature")
    @Builder.Default
    private Set<VehicleFeature> vehicleFeatures = new HashSet<>();

    // --- Ratings & Status ---
    @Builder.Default
    private Double rating = 5.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DriverStatus status = DriverStatus.PENDING_APPROVAL;

    @Builder.Default
    private Boolean isOnline = false;

    private String rejectionReason;
    private LocalDateTime reviewedAt;
    private Long reviewedByAdminId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}