package com.bracit.voucher_engine.dto;

import com.bracit.voucher_engine.model.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDto {
    private Long id;
    private String voucherNumber;
    private LocalDate voucherDate;
    private BigDecimal amount;
    private String description;
    private String createdBy;
    private LocalDate createdDate;
    private String approvedBy;
    private LocalDate approvedDate;
    private VoucherStatus status;
} 