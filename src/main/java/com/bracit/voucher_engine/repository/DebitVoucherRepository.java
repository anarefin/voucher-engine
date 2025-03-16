package com.bracit.voucher_engine.repository;

import com.bracit.voucher_engine.model.DebitVoucher;
import com.bracit.voucher_engine.model.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DebitVoucherRepository extends JpaRepository<DebitVoucher, Long> {
    Optional<DebitVoucher> findByVoucherNumber(String voucherNumber);
    List<DebitVoucher> findByStatus(VoucherStatus status);
    List<DebitVoucher> findByVoucherDateBetween(LocalDate startDate, LocalDate endDate);
    List<DebitVoucher> findByCreatedBy(String createdBy);
} 