package com.bracit.voucher_engine.controller;

import com.bracit.voucher_engine.dto.DebitVoucherDto;
import com.bracit.voucher_engine.model.VoucherStatus;
import com.bracit.voucher_engine.service.DebitVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/debit-vouchers")
public class DebitVoucherController {

    private final DebitVoucherService debitVoucherService;

    @Autowired
    public DebitVoucherController(DebitVoucherService debitVoucherService) {
        this.debitVoucherService = debitVoucherService;
    }

    @PostMapping
    public ResponseEntity<DebitVoucherDto> createVoucher(@RequestBody DebitVoucherDto voucherDto) {
        return new ResponseEntity<>(debitVoucherService.createVoucher(voucherDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DebitVoucherDto> getVoucherById(@PathVariable Long id) {
        return ResponseEntity.ok(debitVoucherService.getVoucherById(id));
    }

    @GetMapping("/number/{voucherNumber}")
    public ResponseEntity<DebitVoucherDto> getVoucherByNumber(@PathVariable String voucherNumber) {
        return ResponseEntity.ok(debitVoucherService.getVoucherByNumber(voucherNumber));
    }

    @GetMapping
    public ResponseEntity<List<DebitVoucherDto>> getAllVouchers() {
        return ResponseEntity.ok(debitVoucherService.getAllVouchers());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DebitVoucherDto>> getVouchersByStatus(@PathVariable VoucherStatus status) {
        return ResponseEntity.ok(debitVoucherService.getVouchersByStatus(status));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<DebitVoucherDto>> getVouchersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(debitVoucherService.getVouchersByDateRange(startDate, endDate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DebitVoucherDto> updateVoucher(
            @PathVariable Long id,
            @RequestBody DebitVoucherDto voucherDto) {
        return ResponseEntity.ok(debitVoucherService.updateVoucher(id, voucherDto));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<DebitVoucherDto> approveVoucher(
            @PathVariable Long id,
            @RequestParam String approvedBy) {
        return ResponseEntity.ok(debitVoucherService.approveVoucher(id, approvedBy));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<DebitVoucherDto> rejectVoucher(
            @PathVariable Long id,
            @RequestParam String rejectedBy) {
        return ResponseEntity.ok(debitVoucherService.rejectVoucher(id, rejectedBy));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        debitVoucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }
} 