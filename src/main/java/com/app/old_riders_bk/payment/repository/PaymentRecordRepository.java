package com.app.old_riders_bk.payment.repository;

import com.app.old_riders_bk.payment.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    Optional<PaymentRecord> findByReference(String reference);
}