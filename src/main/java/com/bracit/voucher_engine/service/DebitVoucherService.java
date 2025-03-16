package com.bracit.voucher_engine.service;

import com.bracit.voucher_engine.dto.DebitVoucherDto;
import com.bracit.voucher_engine.model.VoucherStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DebitVoucherService {
    DebitVoucherDto createVoucher(DebitVoucherDto voucherDto);
    List<DebitVoucherDto> createVouchersBulk(List<DebitVoucherDto> voucherDtos);
    DebitVoucherDto getVoucherById(Long id);
    CompletableFuture<DebitVoucherDto> getVoucherByIdAsync(Long id);
    DebitVoucherDto getVoucherByNumber(String voucherNumber);
    List<DebitVoucherDto> getAllVouchers();
    List<DebitVoucherDto> getVouchersByStatus(VoucherStatus status);
    List<DebitVoucherDto> getVouchersByDateRange(LocalDate startDate, LocalDate endDate);
    DebitVoucherDto updateVoucher(Long id, DebitVoucherDto voucherDto);
    DebitVoucherDto approveVoucher(Long id, String approvedBy);
    DebitVoucherDto rejectVoucher(Long id, String rejectedBy);
    void deleteVoucher(Long id);
} 