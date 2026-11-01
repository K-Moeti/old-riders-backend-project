package com.app.old_riders_bk.driver.service;

import com.app.old_riders_bk.driver.dto.DriverDtos.*;
import com.app.old_riders_bk.driver.entity.*;
import com.app.old_riders_bk.driver.repository.DriverRepository;
import com.app.old_riders_bk.driver.repository.DriverShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverShiftRepository shiftRepository;
    private final PasswordEncoder passwordEncoder;

    // ==========================================
    // --- 1. REGISTRATION & AUTH ---
    // ==========================================

    @Transactional
    public DriverResponse registerDriver(DriverRegisterRequest request) {
        if (driverRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        if (driverRepository.existsByPhoneNumber(request.getMobileNumber())) {
            throw new IllegalArgumentException("Mobile number is already registered.");
        }

        if (!Boolean.TRUE.equals(request.getAgreedToTerms())) {
            throw new IllegalArgumentException("You must agree to the terms and conditions.");
        }

        DriverAddress address = DriverAddress.builder()
                .streetAddress(request.getStreetAddress())
                .suburb(request.getSuburb())
                .cityOrTown(request.getCityOrTown())
                .postalCode(request.getPostalCode())
                .build();

        DriverDocuments documents = DriverDocuments.builder()
                .idDocumentUrl(request.getIdDocumentUrl())
                .driversLicenceFrontUrl(request.getDriversLicenceFrontUrl())
                .driversLicenceBackUrl(request.getDriversLicenceBackUrl())
                .vehicleLicenceDiscUrl(request.getVehicleLicenceDiscUrl())
                .licenceDiscExpiryDate(request.getLicenceDiscExpiryDate())
                .roadworthyCertificateUrl(request.getRoadworthyCertificateUrl())
                .roadworthyExpiryDate(request.getRoadworthyExpiryDate())
                .insuranceDocumentUrl(request.getInsuranceDocumentUrl())
                .insuranceExpiryDate(request.getInsuranceExpiryDate())
                .build();

        Driver driver = Driver.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getMobileNumber())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .address(address)
                .documents(documents)
                .agreedToTerms(request.getAgreedToTerms())
                .vehicleModel(request.getVehicleModel())
                .vehicleColor(request.getVehicleColor())
                .registrationPlate(request.getRegistrationPlate())
                .seatingCapacity(request.getSeatingCapacity())
                .isWheelchairFriendly(Boolean.TRUE.equals(request.getIsWheelchairFriendly()))
                .vehicleFeatures(request.getVehicleFeatures() != null ? request.getVehicleFeatures() : new HashSet<>())
                .status(DriverStatus.PENDING_APPROVAL)
                .rating(5.0)
                .isOnline(false)
                .build();

        return mapToResponse(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public DriverResponse login(DriverLoginRequest request) {
        Driver driver = driverRepository.findByEmail(request.getEmailOrPhone())
                .or(() -> driverRepository.findByPhoneNumber(request.getEmailOrPhone()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid email/phone or password."));

        if (!passwordEncoder.matches(request.getPassword(), driver.getPassword())) {
            throw new IllegalArgumentException("Invalid email/phone or password.");
        }

        return mapToResponse(driver);
    }

    // ==========================================
    // --- 2. DRIVER PROFILE CRUD ---
    // ==========================================

    @Transactional(readOnly = true)
    public DriverResponse getDriverProfile(Long driverId) {
        return mapToResponse(findDriver(driverId));
    }

    @Transactional
    public DriverResponse updateProfile(Long driverId, UpdateDriverProfileRequest request) {
        Driver driver = findDriver(driverId);

        if (!driver.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (driverRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email is already taken by another account.");
            }
            driver.setEmail(request.getEmail());
        }

        if (!driver.getPhoneNumber().equals(request.getMobileNumber())) {
            if (driverRepository.existsByPhoneNumber(request.getMobileNumber())) {
                throw new IllegalArgumentException("Mobile number is already taken by another account.");
            }
            driver.setPhoneNumber(request.getMobileNumber());
        }

        driver.setFullName(request.getFullName());

        DriverAddress address = DriverAddress.builder()
                .streetAddress(request.getStreetAddress())
                .suburb(request.getSuburb())
                .cityOrTown(request.getCityOrTown())
                .postalCode(request.getPostalCode())
                .build();
        driver.setAddress(address);

        return mapToResponse(driverRepository.save(driver));
    }

    @Transactional
    public void changePassword(Long driverId, ChangeDriverPasswordRequest request) {
        Driver driver = findDriver(driverId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), driver.getPassword())) {
            throw new IllegalArgumentException("Current password does not match.");
        }

        driver.setPassword(passwordEncoder.encode(request.getNewPassword()));
        driverRepository.save(driver);
    }

    @Transactional
    public void deleteDriver(Long driverId) {
        Driver driver = findDriver(driverId);
        driver.setStatus(DriverStatus.SUSPENDED);
        driver.setIsOnline(false);

        shiftRepository.findTopByDriverIdAndEndedAtIsNullOrderByStartedAtDesc(driverId)
                .ifPresent(shift -> {
                    shift.setEndedAt(LocalDateTime.now());
                    shiftRepository.save(shift);
                });

        driverRepository.save(driver);
    }

    // ==========================================
    // --- 3. "MY VEHICLE" CRUD OPERATIONS ---
    // ==========================================

    // READ
    @Transactional(readOnly = true)
    public MyVehicleResponse getMyVehicle(Long driverId) {
        Driver driver = findDriver(driverId);
        return mapToMyVehicleResponse(driver);
    }

    // CREATE / FULL UPDATE (PUT)
    @Transactional
    public MyVehicleResponse saveOrUpdateMyVehicle(Long driverId, SaveMyVehicleRequest request) {
        Driver driver = findDriver(driverId);

        driver.setVehicleModel(request.getVehicleModel());
        driver.setVehicleColor(request.getVehicleColor());
        driver.setRegistrationPlate(request.getRegistrationPlate());
        driver.setSeatingCapacity(request.getSeatingCapacity());
        driver.setIsWheelchairFriendly(Boolean.TRUE.equals(request.getIsWheelchairFriendly()));
        driver.setVehicleFeatures(request.getVehicleFeatures() != null ? request.getVehicleFeatures() : new HashSet<>());

        DriverDocuments docs = driver.getDocuments() != null ? driver.getDocuments() : new DriverDocuments();
        docs.setVehicleLicenceDiscUrl(request.getVehicleLicenceDiscUrl());
        docs.setLicenceDiscExpiryDate(request.getLicenceDiscExpiryDate());
        docs.setRoadworthyCertificateUrl(request.getRoadworthyCertificateUrl());
        docs.setRoadworthyExpiryDate(request.getRoadworthyExpiryDate());
        docs.setInsuranceDocumentUrl(request.getInsuranceDocumentUrl());
        docs.setInsuranceExpiryDate(request.getInsuranceExpiryDate());
        driver.setDocuments(docs);

        driver.setStatus(DriverStatus.PENDING_APPROVAL);

        return mapToMyVehicleResponse(driverRepository.save(driver));
    }

    // PATCH: Vehicle Specs Only (Color, Seats, Plate, Wheelchair)
    @Transactional
    public MyVehicleResponse patchVehicleSpecs(Long driverId, PatchVehicleSpecsRequest request) {
        Driver driver = findDriver(driverId);

        if (request.getVehicleModel() != null && !request.getVehicleModel().isBlank()) {
            driver.setVehicleModel(request.getVehicleModel());
        }
        if (request.getVehicleColor() != null && !request.getVehicleColor().isBlank()) {
            driver.setVehicleColor(request.getVehicleColor());
        }
        if (request.getRegistrationPlate() != null && !request.getRegistrationPlate().isBlank()) {
            driver.setRegistrationPlate(request.getRegistrationPlate());
        }
        if (request.getSeatingCapacity() != null) {
            driver.setSeatingCapacity(request.getSeatingCapacity());
        }
        if (request.getIsWheelchairFriendly() != null) {
            driver.setIsWheelchairFriendly(request.getIsWheelchairFriendly());
        }

        return mapToMyVehicleResponse(driverRepository.save(driver));
    }

    // PATCH: Features Only (A/C, Wheelchair Ramp, USB, etc.)
    @Transactional
    public MyVehicleResponse updateVehicleFeatures(Long driverId, UpdateVehicleFeaturesRequest request) {
        Driver driver = findDriver(driverId);
        driver.setVehicleFeatures(request.getVehicleFeatures() != null ? request.getVehicleFeatures() : new HashSet<>());
        return mapToMyVehicleResponse(driverRepository.save(driver));
    }

    // PATCH: Compliance Documents & Expiry Dates Only
    @Transactional
    public MyVehicleResponse updateVehicleCompliance(Long driverId, UpdateVehicleComplianceRequest request) {
        Driver driver = findDriver(driverId);
        DriverDocuments docs = driver.getDocuments() != null ? driver.getDocuments() : new DriverDocuments();

        if (request.getVehicleLicenceDiscUrl() != null && !request.getVehicleLicenceDiscUrl().isBlank()) {
            docs.setVehicleLicenceDiscUrl(request.getVehicleLicenceDiscUrl());
        }
        if (request.getLicenceDiscExpiryDate() != null) {
            docs.setLicenceDiscExpiryDate(request.getLicenceDiscExpiryDate());
        }
        if (request.getRoadworthyCertificateUrl() != null) {
            docs.setRoadworthyCertificateUrl(request.getRoadworthyCertificateUrl());
        }
        if (request.getRoadworthyExpiryDate() != null) {
            docs.setRoadworthyExpiryDate(request.getRoadworthyExpiryDate());
        }
        if (request.getInsuranceDocumentUrl() != null) {
            docs.setInsuranceDocumentUrl(request.getInsuranceDocumentUrl());
        }
        if (request.getInsuranceExpiryDate() != null) {
            docs.setInsuranceExpiryDate(request.getInsuranceExpiryDate());
        }

        driver.setDocuments(docs);
        driver.setStatus(DriverStatus.PENDING_APPROVAL);

        return mapToMyVehicleResponse(driverRepository.save(driver));
    }

    // DELETE: Remove Vehicle Details
    @Transactional
    public void deleteVehicle(Long driverId) {
        Driver driver = findDriver(driverId);

        driver.setVehicleModel(null);
        driver.setVehicleColor(null);
        driver.setRegistrationPlate(null);
        driver.setSeatingCapacity(null);
        driver.setIsWheelchairFriendly(false);
        driver.getVehicleFeatures().clear();

        if (driver.getDocuments() != null) {
            driver.getDocuments().setVehicleLicenceDiscUrl(null);
            driver.getDocuments().setLicenceDiscExpiryDate(null);
            driver.getDocuments().setRoadworthyCertificateUrl(null);
            driver.getDocuments().setRoadworthyExpiryDate(null);
            driver.getDocuments().setInsuranceDocumentUrl(null);
            driver.getDocuments().setInsuranceExpiryDate(null);
        }

        driver.setIsOnline(false);
        driver.setStatus(DriverStatus.DOCUMENT_REUPLOAD_REQUESTED);
        driverRepository.save(driver);
    }

    // ==========================================
    // --- 4. DRIVER LICENCE / ID RENEWAL ---
    // ==========================================

    @Transactional
    public DriverResponse updateDocuments(Long driverId, UpdateDriverDocumentsRequest request) {
        Driver driver = findDriver(driverId);

        DriverDocuments documents = DriverDocuments.builder()
                .idDocumentUrl(request.getIdDocumentUrl())
                .driversLicenceFrontUrl(request.getDriversLicenceFrontUrl())
                .driversLicenceBackUrl(request.getDriversLicenceBackUrl())
                .vehicleLicenceDiscUrl(request.getVehicleLicenceDiscUrl())
                .licenceDiscExpiryDate(request.getLicenceDiscExpiryDate())
                .roadworthyCertificateUrl(request.getRoadworthyCertificateUrl())
                .roadworthyExpiryDate(request.getRoadworthyExpiryDate())
                .insuranceDocumentUrl(request.getInsuranceDocumentUrl())
                .insuranceExpiryDate(request.getInsuranceExpiryDate())
                .build();
        driver.setDocuments(documents);

        driver.setStatus(DriverStatus.PENDING_APPROVAL);

        return mapToResponse(driverRepository.save(driver));
    }

    // ==========================================
    // --- 5. ONLINE SHIFT MANAGEMENT ---
    // ==========================================

    @Transactional
    public DriverResponse updateOnlineStatus(Long driverId, Boolean isOnline) {
        Driver driver = findDriver(driverId);

        if (Boolean.TRUE.equals(isOnline) && driver.getStatus() != DriverStatus.APPROVED) {
            throw new IllegalStateException("Your account is " + driver.getStatus() + ". Only APPROVED drivers can go online.");
        }

        driver.setIsOnline(isOnline);

        if (Boolean.TRUE.equals(isOnline)) {
            shiftRepository.findTopByDriverIdAndEndedAtIsNullOrderByStartedAtDesc(driverId)
                    .ifPresentOrElse(shift -> {}, () -> {
                        DriverShift newShift = DriverShift.builder()
                                .driverId(driverId)
                                .startedAt(LocalDateTime.now())
                                .build();
                        shiftRepository.save(newShift);
                    });
        } else {
            shiftRepository.findTopByDriverIdAndEndedAtIsNullOrderByStartedAtDesc(driverId)
                    .ifPresent(activeShift -> {
                        activeShift.setEndedAt(LocalDateTime.now());
                        shiftRepository.save(activeShift);
                    });
        }

        return mapToResponse(driverRepository.save(driver));
    }

    // ==========================================
    // --- PRIVATE MAPPERS & HELPERS ---
    // ==========================================

    private Driver findDriver(Long driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + driverId));
    }

    private MyVehicleResponse mapToMyVehicleResponse(Driver driver) {
        DriverDocuments docs = driver.getDocuments();
        LocalDate today = LocalDate.now();

        boolean hasRoadworthy = docs != null && docs.getRoadworthyCertificateUrl() != null && !docs.getRoadworthyCertificateUrl().isBlank();
        boolean hasInsurance = docs != null && docs.getInsuranceDocumentUrl() != null && !docs.getInsuranceDocumentUrl().isBlank();

        return MyVehicleResponse.builder()
                .driverId(driver.getId())
                .vehicleModel(driver.getVehicleModel())
                .vehicleColor(driver.getVehicleColor())
                .registrationPlate(driver.getRegistrationPlate())
                .seatingCapacity(driver.getSeatingCapacity())
                .isWheelchairFriendly(driver.getIsWheelchairFriendly())
                .vehicleFeatures(driver.getVehicleFeatures())
                .vehicleLicenceDiscUrl(docs != null ? docs.getVehicleLicenceDiscUrl() : null)
                .licenceDiscExpiryDate(docs != null ? docs.getLicenceDiscExpiryDate() : null)
                .isLicenceDiscExpired(docs != null && docs.getLicenceDiscExpiryDate() != null && docs.getLicenceDiscExpiryDate().isBefore(today))
                .roadworthyCertificateUrl(hasRoadworthy ? docs.getRoadworthyCertificateUrl() : null)
                .roadworthyExpiryDate(hasRoadworthy ? docs.getRoadworthyExpiryDate() : null)
                .isRoadworthyExpired(hasRoadworthy && docs.getRoadworthyExpiryDate() != null && docs.getRoadworthyExpiryDate().isBefore(today))
                .hasRoadworthy(hasRoadworthy)
                .insuranceDocumentUrl(hasInsurance ? docs.getInsuranceDocumentUrl() : null)
                .insuranceExpiryDate(hasInsurance ? docs.getInsuranceExpiryDate() : null)
                .hasInsurance(hasInsurance)
                .build();
    }

    private DriverResponse mapToResponse(Driver driver) {
        DriverAddress address = driver.getAddress();
        DriverDocuments documents = driver.getDocuments();

        return DriverResponse.builder()
                .driverId(driver.getId())
                .fullName(driver.getFullName())
                .mobileNumber(driver.getPhoneNumber())
                .email(driver.getEmail())
                .streetAddress(address != null ? address.getStreetAddress() : null)
                .suburb(address != null ? address.getSuburb() : null)
                .cityOrTown(address != null ? address.getCityOrTown() : null)
                .postalCode(address != null ? address.getPostalCode() : null)
                .vehicleModel(driver.getVehicleModel())
                .vehicleColor(driver.getVehicleColor())
                .registrationPlate(driver.getRegistrationPlate())
                .seatingCapacity(driver.getSeatingCapacity())
                .isWheelchairFriendly(driver.getIsWheelchairFriendly())
                .vehicleFeatures(driver.getVehicleFeatures())
                .idDocumentUrl(documents != null ? documents.getIdDocumentUrl() : null)
                .driversLicenceFrontUrl(documents != null ? documents.getDriversLicenceFrontUrl() : null)
                .driversLicenceBackUrl(documents != null ? documents.getDriversLicenceBackUrl() : null)
                .vehicleLicenceDiscUrl(documents != null ? documents.getVehicleLicenceDiscUrl() : null)
                .licenceDiscExpiryDate(documents != null ? documents.getLicenceDiscExpiryDate() : null)
                .roadworthyCertificateUrl(documents != null ? documents.getRoadworthyCertificateUrl() : null)
                .roadworthyExpiryDate(documents != null ? documents.getRoadworthyExpiryDate() : null)
                .insuranceDocumentUrl(documents != null ? documents.getInsuranceDocumentUrl() : null)
                .insuranceExpiryDate(documents != null ? documents.getInsuranceExpiryDate() : null)
                .agreedToTerms(driver.getAgreedToTerms())
                .rating(driver.getRating())
                .status(driver.getStatus())
                .isOnline(driver.getIsOnline())
                .createdAt(driver.getCreatedAt())
                .build();
    }
}