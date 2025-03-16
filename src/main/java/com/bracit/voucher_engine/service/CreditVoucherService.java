package com.bracit.voucher_engine.service;

import com.bracit.voucher_engine.dto.CreditVoucherDto;
import com.bracit.voucher_engine.model.VoucherStatus;

import java.time.LocalDate;
import java.util.List;

public interface CreditVoucherService {
    CreditVoucherDto createVoucher(CreditVoucherDto voucherDto);
    CreditVoucherDto getVoucherById(Long id);
    CreditVoucherDto getVoucherByNumber(String voucherNumber);
    List<CreditVoucherDto> getAllVouchers();
    List<CreditVoucherDto> getVouchersByStatus(VoucherStatus status);
    List<CreditVoucherDto> getVouchersByDateRange(LocalDate startDate, LocalDate endDate);
    CreditVoucherDto updateVoucher(Long id, CreditVoucherDto voucherDto);
    CreditVoucherDto approveVoucher(Long id, String approvedBy);
    CreditVoucherDto rejectVoucher(Long id, String rejectedBy);
    void deleteVoucher(Long id);
} 