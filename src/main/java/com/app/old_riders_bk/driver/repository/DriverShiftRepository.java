package com.app.old_riders_bk.driver.repository;

import com.app.old_riders_bk.driver.entity.DriverShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverShiftRepository extends JpaRepository<DriverShift, Long> {
    Optional<DriverShift> findTopByDriverIdAndEndedAtIsNullOrderByStartedAtDesc(Long driverId);
    List<DriverShift> findByDriverIdAndStartedAtGreaterThanEqual(Long driverId, LocalDateTime start);
}