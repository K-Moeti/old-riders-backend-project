package com.app.old_riders_bk.user.service;

import com.app.old_riders_bk.user.dto.RiderProfileDtos.*;
import com.app.old_riders_bk.user.entity.HomeAddress;
import com.app.old_riders_bk.user.entity.Rider;
import com.app.old_riders_bk.user.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiderProfileService {

    private final RiderRepository riderRepository;

    @Transactional(readOnly = true)
    public RiderProfileResponse getProfile(Long riderId) {
        Rider rider = findRider(riderId);
        return mapToResponse(rider);
    }

    @Transactional
    public RiderProfileResponse updateProfile(Long riderId, UpdateRiderProfileRequest request) {
        Rider rider = findRider(riderId);

        // Check if phone number is changed and already taken by someone else
        if (!rider.getPhoneNumber().equals(request.getPhoneNumber())) {
            if (riderRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new IllegalArgumentException("Phone number is already associated with another account.");
            }
            rider.setPhoneNumber(request.getPhoneNumber());
        }

        rider.setFullName(request.getFullName());
        rider.setEmergencyPhoneNumber(request.getEmergencyPhoneNumber());
        rider.setEmergencyContactName(request.getEmergencyContactName());

        HomeAddress address = HomeAddress.builder()
                .street(request.getStreet())
                .homeNumber(request.getHomeNumber())
                .province(request.getProvince())
                .postalCode(request.getPostalCode())
                .build();
        rider.setHomeAddress(address);

        rider.setMobilityNeeds(request.getMobilityNeeds());
        rider.setShareRideWithFamily(request.getShareRideWithFamily());
        rider.setPreferredPaymentMethod(request.getPreferredPaymentMethod());

        return mapToResponse(riderRepository.save(rider));
    }

    @Transactional
    public RiderProfileResponse toggleShareRide(Long riderId, Boolean shareRideWithFamily) {
        Rider rider = findRider(riderId);
        rider.setShareRideWithFamily(shareRideWithFamily);
        return mapToResponse(riderRepository.save(rider));
    }

    @Transactional
    public RiderProfileResponse updateMobilityNeeds(Long riderId, UpdateMobilityNeedsRequest request) {
        Rider rider = findRider(riderId);
        rider.setMobilityNeeds(request.getMobilityNeeds());
        return mapToResponse(riderRepository.save(rider));
    }

    @Transactional
    public RiderProfileResponse updatePaymentMethod(Long riderId, UpdatePaymentMethodRequest request) {
        Rider rider = findRider(riderId);
        rider.setPreferredPaymentMethod(request.getPreferredPaymentMethod());
        return mapToResponse(riderRepository.save(rider));
    }

    @Transactional
    public void deleteOrDisableAccount(Long riderId) {
        Rider rider = findRider(riderId);
        rider.setIsDisabled(true);
        riderRepository.save(rider);
    }

    private Rider findRider(Long riderId) {
        return riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("Rider not found with ID: " + riderId));
    }

    private RiderProfileResponse mapToResponse(Rider rider) {
        HomeAddress address = rider.getHomeAddress();
        return RiderProfileResponse.builder()
                .id(rider.getId())
                .fullName(rider.getFullName())
                .phoneNumber(rider.getPhoneNumber())
                .emergencyPhoneNumber(rider.getEmergencyPhoneNumber())
                .emergencyContactName(rider.getEmergencyContactName())
                .street(address != null ? address.getStreet() : null)
                .homeNumber(address != null ? address.getHomeNumber() : null)
                .province(address != null ? address.getProvince() : null)
                .postalCode(address != null ? address.getPostalCode() : null)
                .mobilityNeeds(rider.getMobilityNeeds())
                .shareRideWithFamily(rider.getShareRideWithFamily())
                .preferredPaymentMethod(rider.getPreferredPaymentMethod())
                .isDisabled(rider.getIsDisabled())
                .createdAt(rider.getCreatedAt())
                .updatedAt(rider.getUpdatedAt())
                .build();
    }
}