package com.app.old_riders_bk.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeAddress {

    @Column(name = "street_name")
    private String street;

    @Column(name = "house_number")
    private String homeNumber;

    @Column(name = "province_area")
    private String province; // Area / Province

    @Column(name = "postal_code")
    private String postalCode;
}