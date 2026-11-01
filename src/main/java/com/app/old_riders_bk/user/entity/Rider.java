package com.app.old_riders_bk.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "riders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 20)
    private String emergencyPhoneNumber;

    private String emergencyContactName; // Optional family contact name

    @Embedded
    private HomeAddress homeAddress;

    // --- How can we help you ride ---
    @ElementCollection(targetClass = MobilityAssistance.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "rider_mobility_needs", joinColumns = @JoinColumn(name = "rider_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "mobility_need")
    @Builder.Default
    private Set<MobilityAssistance> mobilityNeeds = new HashSet<>();

    // --- Safety Feature: Share ride with family ---
    @Column(nullable = false)
    @Builder.Default
    private Boolean shareRideWithFamily = false;

    // --- Payment Preference ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentMethod preferredPaymentMethod = PaymentMethod.CASH;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDisabled = false;

    @Column(length = 6)
    private String otpCode;

    private LocalDateTime otpExpiry;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}