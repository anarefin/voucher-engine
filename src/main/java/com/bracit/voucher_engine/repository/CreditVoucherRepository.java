package com.bracit.voucher_engine.repository;

import com.bracit.voucher_engine.model.CreditVoucher;
import com.bracit.voucher_engine.model.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditVoucherRepository extends JpaRepository<CreditVoucher, Long> {
    Optional<CreditVoucher> findByVoucherNumber(String voucherNumber);
    List<CreditVoucher> findByStatus(VoucherStatus status);
    List<CreditVoucher> findByVoucherDateBetween(LocalDate startDate, LocalDate endDate);
    List<CreditVoucher> findByCreatedBy(String createdBy);
} 