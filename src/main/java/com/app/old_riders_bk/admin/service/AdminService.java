package com.app.old_riders_bk.admin.service;

import com.app.old_riders_bk.admin.dto.AdminDtos.*;
import com.app.old_riders_bk.admin.entity.Admin;
import com.app.old_riders_bk.admin.entity.AdminRole;
import com.app.old_riders_bk.admin.repository.AdminRepository;
import com.app.old_riders_bk.booking.entity.BookingStatus;
import com.app.old_riders_bk.booking.entity.RideBooking;
import com.app.old_riders_bk.booking.repository.RideBookingRepository;
import com.app.old_riders_bk.common.service.DriverEmailNotificationService;
import com.app.old_riders_bk.driver.entity.Driver;
import com.app.old_riders_bk.driver.entity.DriverStatus;
import com.app.old_riders_bk.driver.repository.DriverRepository;
import com.app.old_riders_bk.user.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final DriverRepository driverRepository;
    private final RiderRepository riderRepository;
    private final RideBookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final DriverEmailNotificationService emailNotificationService;

    // --- Admin Auth & Management ---

    @Transactional
    public AdminResponse createAdmin(Long requestingAdminId, CreateAdminRequest request) {
        Admin requester = adminRepository.findById(requestingAdminId)
                .orElseThrow(() -> new IllegalArgumentException("Requester admin not found."));

        if (requester.getRole() != AdminRole.SUPER_ADMIN) {
            throw new IllegalStateException("Only SUPER_ADMIN can create admin accounts.");
        }

        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Admin with this email already exists.");
        }

        Admin newAdmin = Admin.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();

        return mapToAdminResponse(adminRepository.save(newAdmin));
    }

    @Transactional(readOnly = true)
    public AdminResponse login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid admin credentials."));

        if (!Boolean.TRUE.equals(admin.getIsActive())) {
            throw new IllegalStateException("Admin account has been deactivated.");
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("Invalid admin credentials.");
        }

        return mapToAdminResponse(admin);
    }

    // --- Driver Document Review & Approval (with Automated Email) ---

    @Transactional(readOnly = true)
    public List<DriverDocumentReviewCard> getDriversByStatus(DriverStatus status) {
        List<Driver> drivers = (status != null)
                ? driverRepository.findByStatusOrderByCreatedAtAsc(status)
                : driverRepository.findAll();

        return drivers.stream().map(this::mapToReviewCard).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DriverDocumentReviewCard getDriverDetailsForReview(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + driverId));
        return mapToReviewCard(driver);
    }

    @Transactional
    public DriverDocumentReviewCard reviewDriverDocuments(Long driverId, ReviewDriverRequest request) {
        Admin admin = adminRepository.findById(request.getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        if (!Boolean.TRUE.equals(admin.getIsActive())) {
            throw new IllegalStateException("Inactive admin cannot perform reviews.");
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + driverId));

        DriverStatus decision = request.getDecision();

        // 1. Validate decision
        if (decision != DriverStatus.APPROVED &&
                decision != DriverStatus.REJECTED &&
                decision != DriverStatus.DOCUMENT_REUPLOAD_REQUESTED) {
            throw new IllegalArgumentException("Decision must be APPROVED, REJECTED, or DOCUMENT_REUPLOAD_REQUESTED.");
        }

        // 2. Validate reason if rejected or re-upload requested
        if ((decision == DriverStatus.REJECTED || decision == DriverStatus.DOCUMENT_REUPLOAD_REQUESTED) &&
                (request.getReason() == null || request.getReason().trim().isEmpty())) {
            throw new IllegalArgumentException("A specific reason note must be provided when rejecting or requesting re-upload.");
        }

        // 3. Update driver status and audit fields
        driver.setStatus(decision);
        driver.setReviewedByAdminId(admin.getId());
        driver.setReviewedAt(LocalDateTime.now());
        driver.setRejectionReason(decision != DriverStatus.APPROVED ? request.getReason().trim() : null);

        // Turn driver offline if not approved
        if (decision != DriverStatus.APPROVED) {
            driver.setIsOnline(false);
        }

        Driver savedDriver = driverRepository.save(driver);

        // 4. Send corresponding automated email notification
        String driverEmail = savedDriver.getEmail();
        String driverName = savedDriver.getFullName();
        String reasonNote = request.getReason() != null ? request.getReason().trim() : "";

        if (decision == DriverStatus.APPROVED) {
            emailNotificationService.sendApprovalEmail(driverEmail, driverName);
        } else if (decision == DriverStatus.DOCUMENT_REUPLOAD_REQUESTED) {
            emailNotificationService.sendReuploadDocumentsEmail(driverEmail, driverName, reasonNote);
        } else if (decision == DriverStatus.REJECTED) {
            emailNotificationService.sendRejectionEmail(driverEmail, driverName, reasonNote);
        }

        return mapToReviewCard(savedDriver);
    }

    // --- Super Admin Dashboard Overview ---

    @Transactional(readOnly = true)
    public SuperAdminDashboardStats getPlatformStats(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        if (admin.getRole() != AdminRole.SUPER_ADMIN) {
            throw new IllegalStateException("Only SUPER_ADMIN can view full platform analytics.");
        }

        long totalRiders = riderRepository.count();
        long totalDrivers = driverRepository.count();
        long pendingDrivers = driverRepository.countByStatus(DriverStatus.PENDING_APPROVAL);
        long approvedDrivers = driverRepository.countByStatus(DriverStatus.APPROVED);

        List<RideBooking> allBookings = bookingRepository.findAll();
        long totalRides = allBookings.size();
        long completedRides = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();

        BigDecimal totalRevenue = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .map(RideBooking::getRidePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SuperAdminDashboardStats.builder()
                .totalRiders(totalRiders)
                .totalDrivers(totalDrivers)
                .pendingApprovalDrivers(pendingDrivers)
                .approvedDrivers(approvedDrivers)
                .totalRides(totalRides)
                .completedRides(completedRides)
                .totalPlatformRevenue(totalRevenue)
                .build();
    }

    private AdminResponse mapToAdminResponse(Admin admin) {
        return AdminResponse.builder()
                .id(admin.getId())
                .fullName(admin.getFullName())
                .email(admin.getEmail())
                .role(admin.getRole())
                .isActive(admin.getIsActive())
                .createdAt(admin.getCreatedAt())
                .build();
    }

    private DriverDocumentReviewCard mapToReviewCard(Driver d) {
        return DriverDocumentReviewCard.builder()
                .driverId(d.getId())
                .fullName(d.getFullName())
                .mobileNumber(d.getPhoneNumber())
                .email(d.getEmail())
                .streetAddress(d.getAddress() != null ? d.getAddress().getStreetAddress() : null)
                .suburb(d.getAddress() != null ? d.getAddress().getSuburb() : null)
                .cityOrTown(d.getAddress() != null ? d.getAddress().getCityOrTown() : null)
                .postalCode(d.getAddress() != null ? d.getAddress().getPostalCode() : null)
                .vehicleModel(d.getVehicleModel())
                .registrationPlate(d.getRegistrationPlate())
                .idDocumentUrl(d.getDocuments() != null ? d.getDocuments().getIdDocumentUrl() : null)
                .driversLicenceFrontUrl(d.getDocuments() != null ? d.getDocuments().getDriversLicenceFrontUrl() : null)
                .driversLicenceBackUrl(d.getDocuments() != null ? d.getDocuments().getDriversLicenceBackUrl() : null)
                .vehicleLicenceDiscUrl(d.getDocuments() != null ? d.getDocuments().getVehicleLicenceDiscUrl() : null)
                .agreedToTerms(d.getAgreedToTerms())
                .status(d.getStatus())
                .rejectionReason(d.getRejectionReason())
                .submittedAt(d.getCreatedAt())
                .reviewedAt(d.getReviewedAt())
                .build();
    }
}