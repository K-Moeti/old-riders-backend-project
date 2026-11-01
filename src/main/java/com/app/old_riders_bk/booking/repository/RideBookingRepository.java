package com.app.old_riders_bk.booking.repository;

import com.app.old_riders_bk.booking.entity.BookingStatus;
import com.app.old_riders_bk.booking.entity.RideBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RideBookingRepository extends JpaRepository<RideBooking, Long> {

    List<RideBooking> findByIsAcceptedFalseOrderByCreatedAtDesc();

    List<RideBooking> findByStatusAndIsAcceptedFalseOrderByCreatedAtDesc(BookingStatus status);

    List<RideBooking> findByDriverIdAndStatusInOrderByScheduledPickupTimeAsc(Long driverId, List<BookingStatus> statuses);

    List<RideBooking> findByDriverIdAndStatusOrderByUpdatedAtDesc(Long driverId, BookingStatus status);

    List<RideBooking> findByRiderIdOrderByCreatedAtDesc(Long riderId);

    // Filter available rides excluding those already declined by this driver
    @Query("SELECT b FROM RideBooking b WHERE b.isAccepted = false AND b.status = :status AND :driverId NOT MEMBER OF b.declinedDriverIds ORDER BY b.createdAt DESC")
    List<RideBooking> findAvailableRidesForDriver(@Param("status") BookingStatus status, @Param("driverId") Long driverId);

    // Earnings queries
    List<RideBooking> findByDriverIdAndStatusAndUpdatedAtGreaterThanEqual(Long driverId, BookingStatus status, LocalDateTime fromDate);

    // Completed rides within a custom time window (e.g. daily, weekly, or monthly aggregations)
    List<RideBooking> findByDriverIdAndStatusAndCompletedAtBetween(Long driverId, BookingStatus status, LocalDateTime start, LocalDateTime end);

    // Total Ready to Payout (all completed rides not yet paid out)
    List<RideBooking> findByDriverIdAndStatusAndIsPaidOutFalse(Long driverId, BookingStatus status);

    // Recent completed payments
    List<RideBooking> findTop10ByDriverIdAndStatusOrderByUpdatedAtDesc(Long driverId, BookingStatus status);

    // Active trip check (checks if driver is currently occupied with an ACCEPTED or IN_PROGRESS ride)
    boolean existsByDriverIdAndStatusIn(Long driverId, List<BookingStatus> statuses);
}