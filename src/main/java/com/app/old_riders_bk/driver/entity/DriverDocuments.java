package com.app.old_riders_bk.driver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDocuments {

    @Column(nullable = false)
    private String idDocumentUrl;

    @Column(nullable = false)
    private String driversLicenceFrontUrl;

    @Column(nullable = false)
    private String driversLicenceBackUrl;

    @Column(nullable = false)
    private String vehicleLicenceDiscUrl;

    @Column(nullable = false)
    private LocalDate licenceDiscExpiryDate;

    // --- Optional Roadworthy ---
    @Column(nullable = true)
    private String roadworthyCertificateUrl;

    @Column(nullable = true)
    private LocalDate roadworthyExpiryDate;

    // --- Optional Insurance ---
    @Column(nullable = true)
    private String insuranceDocumentUrl;

    @Column(nullable = true)
    private LocalDate insuranceExpiryDate;
}