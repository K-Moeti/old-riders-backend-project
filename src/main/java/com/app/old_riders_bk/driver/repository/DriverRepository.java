package com.app.old_riders_bk.driver.repository;

import com.app.old_riders_bk.driver.entity.Driver;
import com.app.old_riders_bk.driver.entity.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByEmail(String email);
    Optional<Driver> findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    // For admin review filtering
    List<Driver> findByStatusOrderByCreatedAtAsc(DriverStatus status);
    long countByStatus(DriverStatus status);
}