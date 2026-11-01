package com.app.old_riders_bk.driver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverAddress {

    @Column(name = "street_address", nullable = false)
    private String streetAddress;

    @Column(name = "suburb", nullable = false)
    private String suburb;

    @Column(name = "city_or_town", nullable = false)
    private String cityOrTown;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;
}