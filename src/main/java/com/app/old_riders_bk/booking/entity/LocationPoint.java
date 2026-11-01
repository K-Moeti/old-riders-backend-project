package com.app.old_riders_bk.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationPoint {

    @Column(nullable = false)
    private String addressText; // Display name e.g., "120 Steve Biko Rd, Pretoria"

    private Double latitude;    // Optional coordinates for mapping
    private Double longitude;
}