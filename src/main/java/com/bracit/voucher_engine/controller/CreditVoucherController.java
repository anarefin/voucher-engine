package com.bracit.voucher_engine.controller;

import com.bracit.voucher_engine.dto.CreditVoucherDto;
import com.bracit.voucher_engine.model.VoucherStatus;
import com.bracit.voucher_engine.service.CreditVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/credit-vouchers")
public class CreditVoucherController {

    private final CreditVoucherService creditVoucherService;

    @Autowired
    public CreditVoucherController(CreditVoucherService creditVoucherService) {
        this.creditVoucherService = creditVoucherService;
    }

    @PostMapping
    public ResponseEntity<CreditVoucherDto> createVoucher(@RequestBody CreditVoucherDto voucherDto) {
        return new ResponseEntity<>(creditVoucherService.createVoucher(voucherDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditVoucherDto> getVoucherById(@PathVariable Long id) {
        return ResponseEntity.ok(creditVoucherService.getVoucherById(id));
    }

    @GetMapping("/number/{voucherNumber}")
    public ResponseEntity<CreditVoucherDto> getVoucherByNumber(@PathVariable String voucherNumber) {
        return ResponseEntity.ok(creditVoucherService.getVoucherByNumber(voucherNumber));
    }

    @GetMapping
    public ResponseEntity<List<CreditVoucherDto>> getAllVouchers() {
        return ResponseEntity.ok(creditVoucherService.getAllVouchers());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CreditVoucherDto>> getVouchersByStatus(@PathVariable VoucherStatus status) {
        return ResponseEntity.ok(creditVoucherService.getVouchersByStatus(status));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<CreditVoucherDto>> getVouchersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(creditVoucherService.getVouchersByDateRange(startDate, endDate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreditVoucherDto> updateVoucher(
            @PathVariable Long id,
            @RequestBody CreditVoucherDto voucherDto) {
        return ResponseEntity.ok(creditVoucherService.updateVoucher(id, voucherDto));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<CreditVoucherDto> approveVoucher(
            @PathVariable Long id,
            @RequestParam String approvedBy) {
        return ResponseEntity.ok(creditVoucherService.approveVoucher(id, approvedBy));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<CreditVoucherDto> rejectVoucher(
            @PathVariable Long id,
            @RequestParam String rejectedBy) {
        return ResponseEntity.ok(creditVoucherService.rejectVoucher(id, rejectedBy));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        creditVoucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }
} 